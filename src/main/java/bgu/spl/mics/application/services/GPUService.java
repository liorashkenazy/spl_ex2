package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.GPU;

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

    public GPUService(String name, String type, Cluster cluster) {
        super(name);
        gpu = new GPU(type);
    }

    @Override
    protected void initialize() {
        subscribeEvent(TrainModelEvent.class,new TrainModelCallback());
        subscribeEvent(TestModelEvent.class,new TestModelCallback());
        subscribeBroadcast(TickBroadcast.class,new TickCallback());
    }

    private class TickCallback implements Callback<TickBroadcast> {
        /**
         * This function should be called every time a tick occurs.
         * <p>
         * @param tickBroadcast the message that was taken from the message queue.
         */
        public void call(TickBroadcast tickBroadcast) {
            gpu.tick();
        }
    }

    private class TrainModelCallback implements Callback<TrainModelEvent> {
        public void call(TrainModelEvent trainModelEvent) {
            //TODO
            gpu.trainModel(trainModelEvent.getModel(), );
        }
    }

    private class TestModelCallback implements Callback<TestModelEvent> {
        public void call(TestModelEvent testModelEvent) {
            gpu.testModel(testModelEvent.getModel());
        }
    }
}
