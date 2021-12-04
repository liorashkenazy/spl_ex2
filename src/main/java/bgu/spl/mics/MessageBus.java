package bgu.spl.mics;

import java.util.LinkedList;
import java.util.List;

/**
 * The message-bus is a shared object used for communication between
 * micro-services.
 * It should be implemented as a thread-safe singleton.
 * The message-bus implementation must be thread-safe as
 * it is shared between all the micro-services in the system.
 * You must not alter any of the given methods of this interface. 
 * You cannot add methods to this interface.
 */
public interface MessageBus {

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * <p>
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     * @POST: isSubscribedToMessage(type, m) == true;
     */
    <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m);

    /**
     * Checks if {@code m} is subscribed to receive {@link Message}s of type {@code type}
     * <p>
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     */
    boolean isSubscribedToMessage(Class<? extends Message> type, MicroService m);

    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     * @param type 	The type to subscribe to.
     * @param m    	The subscribing micro-service.
     * @POST: isSubscribedToMessage(type, m) == true;
     */
    void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m);

    /**
     * Notifies the MessageBus that the event {@code e} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will resolve the {@link Future}
     * object associated with {@link Event} {@code e}.
     * <p>
     * @param <T>    The type of the result expected by the completed event.
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     * @PRE: getEventFuture(e) != NULL;
     * @PRE: getEventFuture(e).isDone() == false;
     * @POST: getEventFuture(e).isDone() == true;
     * @POST: getEventFuture(e).get() == result;
     */
    <T> void complete(Event<T> e, T result);

    /**
     * Returns the {@link Future} object associated with the event {@code e}
     * <p>
     * @param <T>    The type of the result expected by the event.
     * @param e      The event.
     */
    <T> Future<T> getEventFuture(Event<T> e);

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b 	The message to added to the queues.
     * @POST: foreach (m : getSubscribedServices(b.getClass()))
     *          awaitMessage(m) == b;
     * @POST: foreach (m : getServices())
     *          if (!getSubscribedServices(b.getClass()).contains(m))
     *              awaitMessage(m) != b;
     */
    void sendBroadcast(Broadcast b);

    /**
     * returns all of the {@link MicroService} subscribed to this specific {@link Message} {@code type}
     * <p>
     * @param type  The broadcast type
     */
    LinkedList<MicroService> getSubscribedServices(Class<? extends Message> type);

    /**
     * returns all of the {@link MicroService} registered to this MessageBus
     * <p>
     */
    LinkedList<MicroService> getRegisteredServices();

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * micro-services subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * <p>
     * @param <T>    	The type of the result expected by the event and its corresponding future object.
     * @param e     	The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
     * @INV: if (getSubscribedServices(e.getClass()).length() == 0)
     *          sendEvent() == null
     *       else
     *          sendEvent() != null
     * @POST: if (getSubscribedServices(e.getClass()).length() > 1):
     *          getNextServiceForEvent(e.getClass()) != @PRE(getNextServiceForEvent(e.getClass()))
     * @POST: awaitMessage(@PRE(getNextServiceForEvent(e.getClass()) == e
     * @POST: foreach (m : getSubscribedServices(e.getClass())
     *          if (m != (@PRE(getNextServiceForEvent(e.getClass()))
     *              awaitMessage(m) != e
     */
    <T> Future<T> sendEvent(Event<T> e);

    /**
     * Returns the next {@link MicroService} designated to receive {@link Event} of type {@code type}
     * <p>
     * @param <T>     The type of the result expected by the completed event.
     * @param type    The type of event
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
     * @POST: getNextServiceForEvents() == @PRE(getNextServiceForEvents())
     */
    <T> MicroService getNextServiceForEvent(Class<? extends Event<T>> type);

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     * @param m the micro-service to create a queue for.
     * @POST: isRegistered(m) == true
     */
    void register(MicroService m);

    /**
     * Check if a message queue has been allocated for {@link MicroService} {@code m}.
     * <p>
     * @param m the micro-service to check
     */
    boolean isRegistered(MicroService m);


    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     * @param m the micro-service to unregister.
     * @POST: isRegistered(m) == false
     */
    void unregister(MicroService m);

    /**
     * Using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     * @PRE: isRegistered(m) == true
     * @POST: awaitMessage(m) != @PRE(awaitMessage(m))
     */
    Message awaitMessage(MicroService m) throws InterruptedException;
    
}
