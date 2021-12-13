package bgu.spl.mics.application.services;

import java.lang.Thread;
import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
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
	int duration;

	public TimeService(int tick_time, int duration) {
		super("TimeService");
		this.tick_time = tick_time;
		this.duration = duration;
	}

	/**
	 This callback should be called upon every tick, this is the way we retain the "normal" Micro Service flow for this
	 service. Instead of sleeping in a loop, we will sleep, then send a TickBroadcast, then retrieve a message from the
	 queue, but the queue is guaranteed to not be empty, as we just sent a TickBroadcast. That will result in another
	 execution of the callback, thus implementing the loop.
	 */
	private class TickBroadcastCallback implements Callback<TickBroadcast> {

		public void call(TickBroadcast event) {
			duration--;
			// Once duration hits 0 it is time to terminate the program
			if (duration == 0) {
				sendBroadcast(new TerminateBroadcast());
				terminate();
			}
			else {
				try {
					// Sleep for the configured time before sending the next tick
					Thread.sleep(tick_time);
				} catch (InterruptedException ignored) {}
				sendBroadcast(new TickBroadcast());
			}
		}
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, new TickBroadcastCallback());
		sendBroadcast(new TickBroadcast());
	}
}
