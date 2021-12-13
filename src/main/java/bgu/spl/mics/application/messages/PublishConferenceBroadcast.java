package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.HashMap;
import java.util.LinkedList;

public class PublishConferenceBroadcast implements Broadcast {

    final LinkedList<Model> published_models;

    public PublishConferenceBroadcast(ConferenceInformation conference) {
        published_models = conference.getModels();
    }
}
