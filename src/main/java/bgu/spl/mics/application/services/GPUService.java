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
    private boolean is_gpu_currently_training = false;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
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
            if (!is_gpu_currently_training) {
                gpu.trainModel(trainModelEvent.getModel(),new TrainModelFinishCallback());
                is_gpu_currently_training = true;
            }
            else {
                waiting_trainModelEvent.add(trainModelEvent);
            }
        }
    }

    private class TestModelCallback implements Callback<TestModelEvent> {
        public void call(TestModelEvent testModelEvent) {
            if (!is_gpu_currently_training) {
                gpu.testModel(testModelEvent.getModel());
            }
            else {
                waiting_testModelEvent.add(testModelEvent);
            }
        }
    }

    private class TrainModelFinishCallback implements Callback<Model> {
        public void call(Model trained_model) {
            is_gpu_currently_training = false;
            sendBroadcast(new TrainModelFinished(trained_model));
            while (!waiting_testModelEvent.isEmpty()) {
                gpu.testModel(waiting_testModelEvent.poll().getModel());
            }
            if (!waiting_trainModelEvent.isEmpty()) {
                gpu.trainModel(waiting_trainModelEvent.pop().getModel(), new TrainModelFinishCallback());
                is_gpu_currently_training = true;
            }
        }
    }
}
