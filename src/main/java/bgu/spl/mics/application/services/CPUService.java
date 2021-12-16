package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;

/**
 * CPU service is responsible for handling the { DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {

    private CPU cpu;

    public CPUService(String name,CPU cpu) {
        super(name);
        this.cpu = cpu;
    }

    /**
     * This method is called once when the event loop starts.
     * responsible to subscribe the service to {@link TickBroadcast}.
     * @POST: MessageBusImp.getInstance.isSubscribedToMessage(DataPreProcessEvent.class, this) == true
     * @POST: MessageBusImp.getInstance.isSubscribedToMessage(TickBroadcast.class, this) == true
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class,new TickCallback());
        subscribeBroadcast(TerminateBroadcast.class, new TerminateCallback());
        sendBroadcast(new InitializeBroadcast());
    }

    private class TickCallback implements Callback<TickBroadcast> {
        /**
         * This function should be called every time a tick occurs.
         * <p>
         * @param tickBroadcast the message that was taken from the message queue.
         */
        public void call(TickBroadcast tickBroadcast) {
            cpu.tick();
        }
    }

    /**
     * The callback terminates the MicroService
     * */
    public class TerminateCallback implements Callback<TerminateBroadcast> {

        public void call(TerminateBroadcast terminateBroadcast) {
            terminate();
        }
    }
}
