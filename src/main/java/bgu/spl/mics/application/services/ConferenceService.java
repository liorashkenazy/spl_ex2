package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConferenceInformation;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private ConferenceInformation conference;

    /**
     * <p>
     * @param name the name of the conference service.
     * @param conference_name the name of the conference related to this service.
     * @param conference_date the time of the conference related to this service.
     */
    public ConferenceService(String name, String conference_name, int conference_date) {
        super(name);
        this.conference = new ConferenceInformation(conference_name,conference_date);
    }
    /**
     * This method is called once when the event loop starts.
     * responsible to subscribe the service to {@link PublishResultsEvent} and to {@link TickBroadcast}.
     * @POST: MessageBusImp.getInstance.isSubscribedToMessage(PublishResultsEvent.class, this) == true
     * @POST: MessageBusImp.getInstance.isSubscribedToMessage(TickBroadcast.class, this) == true
     */
    @Override
    protected void initialize() {
        subscribeEvent(PublishResultsEvent.class ,new PublishResultCallback());
        subscribeBroadcast(TickBroadcast.class,new TickCallback());
    }

    /**
     * This class defines the {@link Callback} that should be invoked when a message of class type
     * {@link PublishResultsEvent} is taken from the message queue.
     */
    private class PublishResultCallback implements Callback<PublishResultsEvent> {
        /**
         * This function aggregate published models to hashmap of successful models via {@link ConferenceInformation}.
         * @param publishResultsEvent the message that was taken from the message queue.
         */
        public void call(PublishResultsEvent publishResultsEvent) {
            conference.addModelToConference(publishResultsEvent.getModel());
        }
    }

    /**
     * This class defines the {@link Callback} that should be invoked when a message of class type
     * {@link TickBroadcast} is taken from the message queue.
     */
    private class TickCallback implements Callback<TickBroadcast> {
        /**
         * This function should be called every time a tick occurs. Once enough ticks have passed to publish all good
         * results, this function will send the {@link PublishConferenceBroadcast} and then the conference
         * unregisters from the system.
         * <p>
         * @param tickBroadcast the message that was taken from the message queue.
         * @POST: @PRE(conference.getTicksLeft()) - 1 == conference.getTicksLeft()
         * @POST: if (conference.getTicksLeft() == 0):
         *          foreach(ms : MessageBusImp.getInstance.getSubscribedServices(PublishConferenceBroadcast.getClass()))
         *                MessageBusImp.getInstance.awaitMessage(ms) == b;
         * @POST: if (conference.getTicksLeft() == 0):
         *          MessageBusImp.getInstance.isRegistered == false;
         */
        public void call(TickBroadcast tickBroadcast) {
            conference.tick();
            if (conference.getTicksLeft() == 0) {
                sendBroadcast(new PublishConferenceBroadcast(conference));
                terminate();
            }
        }
    }
}
