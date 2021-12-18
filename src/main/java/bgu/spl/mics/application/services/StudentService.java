package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

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
    private boolean is_waiting_for_result = false;

    public StudentService(String name, Student student) {
        super(name);
        this.student = student;
        this.student.setStudentForModels();
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TrainModelFinished.class, new TrainModelCompleteCallback());
        subscribeBroadcast(PublishConferenceBroadcast.class, (published) -> student.readPapers(published.getModels()));
        subscribeBroadcast(TerminateBroadcast.class, (terminateBroadcast) -> terminate());
        // Send TrainModelEvent only if student has model to train
        if(student.getCurrentModel() != null) {
            sendEvent(new TrainModelEvent(student.getCurrentModel()));
        }
        sendBroadcast(new InitializeBroadcast());
    }

    private class TrainModelCompleteCallback implements Callback<TrainModelFinished> {
        public void call(TrainModelFinished event) {
            if (event.getModel().getStudent() == student) {
                student.modelFinished(event.getModel());
                Future<Model.Result> res = sendEvent(new TestModelEvent(event.getModel()));
                if (res != null && res.get() == Model.Result.Good) {
                    sendEvent(new PublishResultsEvent(event.getModel()));
                }
                if (student.getCurrentModel() != null) {
                    sendEvent(new TrainModelEvent(student.getCurrentModel()));
                }
            }
        }
    }
}
