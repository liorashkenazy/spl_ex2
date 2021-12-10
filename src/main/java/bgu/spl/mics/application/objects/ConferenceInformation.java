package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConferenceInformation {

    private String name;
    private int date;
    private HashMap<Student,LinkedList<String>> student_to_models_name;
    private int ticks_left;

    /**
     * <p>
     * @param name the name of the conference.
     * @param date the time of the conference.
     */
    public ConferenceInformation(String name, int date) {
        this.name = name;
        this.date = date;
        this.student_to_models_name = new HashMap<>();
        ticks_left = date;
    }

    /**
     * Aggregate {@code model} to hashmap of successful {@link Model}s.
     * <p>
     * @param model The model to aggregate.
     * @PRE: {@code model} != null
     * @POST: if (model.isResultGood())
     *          student_to_models_name.get(model.getStudent()) != null;
     * @POST: if (model.isResultGood())
     *          student_to_models_name.get(model.getStudent()).contains(model.getName()) == true;
     * @POST: if(!model.isResultGood())
     *          student_to_models_name.containsValue(model) == false;
     *
     */
    public void addModelToConference (Model model) {
        if (model.isResultGood()) {
            Student student = model.getStudent();
            if (student_to_models_name.get(student) == null) {
                student_to_models_name.put(student,new LinkedList<String>());
            }
            student_to_models_name.get(student).add(model.getName());
        }
    }

    /**
     * This function should be called every time a tick occurs.
     * <p>
     * @POST: @PRE(getTicksLeft()) - 1 == getTicksLeft()
     * @POST: getTicksLeft() >= 0
     */
    public void tick() {
        ticks_left --;
    }

    /**
     * This function returns the number of ticks left to publish the successful {@link Model}s
     * <p>
     * @return [int] The number of remaining ticks to send {@link PublishConferenceBroadcast}
     * @INV getTicksLeft() >= 0;
     * @POST: @PRE(getTickLeft()) == getTickLeft()
     */
    public int getTicksLeft () {
        return ticks_left;
    }

    /**
     * <p>
     * @return [HashMap] hashmap that maps between {@link Student} to theirs {@link Model}s name
     * of successful {@link Model}s that will be published via {@link PublishConferenceBroadcast}
     */
    public HashMap<Student,LinkedList<String>> getModels () {
        return student_to_models_name;
    }
}
