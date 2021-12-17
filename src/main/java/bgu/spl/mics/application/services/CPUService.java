package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.InitializeBroadcast;
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
        subscribeBroadcast(TickBroadcast.class,(tickBroadcast) -> cpu.tick());
        subscribeBroadcast(TerminateBroadcast.class, (terminateBroadcast) -> terminate());
        sendBroadcast(new InitializeBroadcast());
    }
}
