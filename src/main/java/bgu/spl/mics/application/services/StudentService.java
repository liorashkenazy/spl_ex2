package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.objects.Model;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    private Student student;

    public StudentService(String name, String student_name, String department, Student.Degree degree, Model[] models) {
        super(name);
        student = new Student(student_name, department, degree, models);
    }

    private class TrainModelCompleteBroadcastCallback implements Callback<TrainModelFinished> {
        public void call(TrainModelFinished event) {
            if (event.getModel().getStudent() == student) {
                student.modelFinished(event.getModel());
                Future<Boolean> res = sendEvent(new TestModelEvent(event.getModel()));
                if (res.get()) {
                    // TODO send publish result event
                }
                if (student.getCurrentModel() != null) {
                    sendEvent(new TrainModelEvent(student.getCurrentModel()));
                }
            }
        }
    }

    // TODO: implement according to Lior's class
    private class PublishConferenceBroadcastCallback {
        public void call() {
            // TODO: Retrieve actual models from the event
            student.readPapers(null);
        }
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TrainModelFinished.class, new TrainModelCompleteBroadcastCallback());
        // TODO subscribe to PublishConferenceBroadcast
    }
}
