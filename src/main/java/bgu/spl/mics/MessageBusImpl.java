package bgu.spl.mics;

import java.util.LinkedList;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> boolean isSubscribedToMessage(Class<? extends Event<T>> type, MicroService m) {
		return false;
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> Future<T> getEventFuture(Event<T> e) {
		return null;
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}

	@Override
	public LinkedList<MicroService> getSubscribedServices(Class<? extends Message> type) {
		return null;
	}

	@Override
	public LinkedList<MicroService> getServices() {
		return null;
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> MicroService getNextServiceForEvent(Class<? extends Event<T>> type) {
		return null;
	}

	@Override
	public void register(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRegistered(MicroService m) {
		return false;
	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	

}
