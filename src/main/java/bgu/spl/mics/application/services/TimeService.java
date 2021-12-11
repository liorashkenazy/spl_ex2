package bgu.spl.mics.application.services;

import java.lang.Thread;
import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	int tick_time;

	public TimeService(int tick_time) {
		super("TimeService");
		this.tick_time = tick_time;
	}

	private class TickBroadcastCallback implements Callback<TickBroadcast> {

		public void call(TickBroadcast event) {
			try {
				Thread.sleep(tick_time);
			} catch (InterruptedException ignored) {
			}
			sendBroadcast(new TickBroadcast());
		}
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, new TickBroadcastCallback());
		sendBroadcast(new TickBroadcast());
	}

}
