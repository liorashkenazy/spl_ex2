package bgu.spl.mics;

import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Model;

public class ConferenceService extends MicroService {

    private ConferenceInformation conference;

    public ConferenceService(String name , ConferenceInformation conference) {
        super(name);
        this.conference = conference;
    }

    protected void initialize() {
        subscribeEvent(PublishResultsEvent.class ,new PublishResultCallback());
        subscribeBroadcast(TickBroadcast.class,new TickCallback());
    }

    public class PublishResultCallback implements Callback<PublishResultsEvent> {

        public void call(PublishResultsEvent publishResultsEvent) {
            conference.addModelToConference(publishResultsEvent.getModel());
        }
    }

    public class TickCallback implements Callback<TickBroadcast> {

        public void call(TickBroadcast tick) {
            conference.tick();
            if (conference.getTicksLeft() == 0) {
                sendBroadcast(new PublishConferenceBroadcast(conference));
                terminate();
            }
        }
    }
}
