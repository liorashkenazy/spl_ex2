package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentHashMap<Event, Future> event_to_result_map;
	private ConcurrentHashMap<Class<? extends Event>, LinkedList<MicroService>> event_to_subscriber_map;
	private ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcast_to_subcriber_map;
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> registered_services;

	private MessageBusImpl() {
		event_to_result_map = new ConcurrentHashMap<Event, Future>();
		event_to_subscriber_map = new ConcurrentHashMap<Class<? extends Event>, LinkedList<MicroService>>();
		broadcast_to_subcriber_map = new ConcurrentHashMap<Class<? extends Broadcast>,
															ConcurrentLinkedQueue<MicroService>>();
		registered_services = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
	}

	public static MessageBusImpl getInstance() {
		// TODO
		return new MessageBusImpl();
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (isRegistered(m) && !isSubscribedToMessage(type, m)) {
			Queue<MicroService> event_queue = event_to_subscriber_map.get(type);
			if (event_queue == null) {
				event_to_subscriber_map.putIfAbsent(type, new LinkedList<MicroService>());
				event_queue = event_to_subscriber_map.get(type);
			}
			synchronized (event_queue) {
				event_queue.add(m);
			}
		}
	}

	@Override
	public boolean isSubscribedToMessage(Class<? extends Message> type, MicroService m) {
		if (Broadcast.class.isAssignableFrom(type)) {
			if (broadcast_to_subcriber_map.get(type) == null) {
				return false;
			}
			return broadcast_to_subcriber_map.get(type).contains(m);
		}
		else {
			Queue<MicroService> event_queue = event_to_subscriber_map.get(type);
			if (event_queue == null) {
				return false;
			}
			synchronized (event_queue) {
				return event_queue.contains(m);
			}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (isRegistered(m) && !isSubscribedToMessage(type, m)) {
			ConcurrentLinkedQueue<MicroService> current_list = broadcast_to_subcriber_map.get(type);
			if (current_list == null) {
				broadcast_to_subcriber_map.putIfAbsent(type, new ConcurrentLinkedQueue<MicroService>());
				current_list = broadcast_to_subcriber_map.get(type);
			}
			current_list.add(m);
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> res = event_to_result_map.get(e);
		if (res != null) {
			res.resolve(result);
			event_to_result_map.remove(e);
		}
	}

	@Override
	public <T> Future<T> getEventFuture(Event<T> e) {
		return event_to_result_map.get(e);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		for (MicroService m : broadcast_to_subcriber_map.get(b.getClass())) {
			LinkedBlockingQueue<Message> messages = registered_services.get(m);
			if (messages != null) {
				messages.add(b);
			}
		}
	}

	@Override
	public LinkedList<MicroService> getSubscribedServices(Class<? extends Message> type) {
		return null;
	}

	@Override
	public LinkedList<MicroService> getRegisteredServices() {
		return new LinkedList<MicroService>(Collections.list(registered_services.keys()));
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		LinkedList<MicroService> event_queue = event_to_subscriber_map.get(e.getClass());
		if (event_queue == null) {
			return null;
		}
		synchronized(event_queue) {
			if (event_queue.isEmpty()) {
				return null;
			}
			Future<T> result = new Future<T>();
			MicroService ms = event_queue.poll();
			registered_services.get(ms).add(e);
			event_to_result_map.put(e, result);
			event_queue.add(ms);
			return result;
		}
	}

	@Override
	public <T> MicroService getNextServiceForEvent(Class<? extends Event<T>> type) {
		if (event_to_subscriber_map.get(type) == null) {
			return null;
		}
		return event_to_subscriber_map.get(type).peek();
	}

	@Override
	public void register(MicroService m) {
		registered_services.putIfAbsent(m, new LinkedBlockingQueue<Message>());
	}

	@Override
	public boolean isRegistered(MicroService m) {
		return registered_services.containsKey(m);
	}

	@Override
	public void unregister(MicroService m) {
		for(ConcurrentLinkedQueue<MicroService> subscribers : broadcast_to_subcriber_map.values()) {
			subscribers.remove(m);
		}
		for (LinkedList<MicroService> event_queue : event_to_subscriber_map.values()) {
			synchronized(event_queue) {
				if (event_queue.contains(m)) {
					event_queue.remove(m);
				}
			}
		}
		registered_services.remove(m);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException, IllegalStateException {
		LinkedBlockingQueue<Message> message_queue = registered_services.get(m);
		if (message_queue == null) {
			throw new IllegalStateException();
		}
		return message_queue.take();
	}
}
