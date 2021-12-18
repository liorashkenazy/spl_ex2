package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;
    private LinkedList<TrainModelEvent> waiting_trainModelEvent = new LinkedList<>();
    private LinkedList<TestModelEvent> waiting_testModelEvent = new LinkedList<>();
    private TrainModelEvent current_train_event;
    private boolean is_gpu_currently_training = false;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        this.current_train_event = null;
    }

    @Override
    protected void initialize() {
        subscribeEvent(TrainModelEvent.class,new TrainModelCallback());
        subscribeEvent(TestModelEvent.class,new TestModelCallback());
        subscribeBroadcast(TickBroadcast.class,(tickBroadcast) -> gpu.tick());
        subscribeBroadcast(TerminateBroadcast.class, (terminateBroadcast) -> terminate());
        sendBroadcast(new InitializeBroadcast());
    }

    private class TrainModelCallback implements Callback<TrainModelEvent> {
        public void call(TrainModelEvent trainModelEvent) {
            if (current_train_event == null) {
                gpu.trainModel(trainModelEvent.getModel(), new TrainModelFinishCallback());
                current_train_event = trainModelEvent;
            }
            else {
                waiting_trainModelEvent.add(trainModelEvent);
            }
        }
    }

    private class TestModelCallback implements Callback<TestModelEvent> {
        public void call(TestModelEvent testModelEvent) {
            if (current_train_event == null) {
                complete(testModelEvent, gpu.testModel(testModelEvent.getModel()));
            }
            else {
                waiting_testModelEvent.add(testModelEvent);
            }
        }
    }

    private class TrainModelFinishCallback implements Callback<Model> {
        public void call(Model trained_model) {
            complete(current_train_event, trained_model);
            current_train_event = null;
            sendBroadcast(new TrainModelFinished(trained_model));
            while (!waiting_testModelEvent.isEmpty()) {
                TestModelEvent event = waiting_testModelEvent.poll();
                complete(event, gpu.testModel(event.getModel()));
            }
            if (!waiting_trainModelEvent.isEmpty()) {
                current_train_event = waiting_trainModelEvent.pop();
                gpu.trainModel(current_train_event.getModel(), new TrainModelFinishCallback());
            }
        }
    }
}
