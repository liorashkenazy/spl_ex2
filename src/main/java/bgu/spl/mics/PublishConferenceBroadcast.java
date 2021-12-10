package bgu.spl.mics;

import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Student;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class PublishConferenceBroadcast implements Broadcast{

    ConferenceInformation conference;

    public PublishConferenceBroadcast (ConferenceInformation conference) {
        this.conference = conference;
    }

    public ConcurrentHashMap<Student, LinkedList<String>> getPublishedModel () {
        return conference.getModels();
    }

}
