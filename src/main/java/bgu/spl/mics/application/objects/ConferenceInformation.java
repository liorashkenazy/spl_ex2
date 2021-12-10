package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConferenceInformation {

    private String name;
    private int date;
    private ConcurrentHashMap<Student,LinkedList<String>> student_to_model_name;
    private int ticks_left;

    public ConferenceInformation(String name, int date) {
        this.name = name;
        this.date = date;
        this.student_to_model_name = new ConcurrentHashMap<>();
        ticks_left = date;
    }

    public void addModelToConference (Model model) {
        if (model.isResultGood()) {
            Student student = model.getStudent();
            if (student_to_model_name.get(student) == null) {
                student_to_model_name.putIfAbsent(student,new LinkedList<>());
            }
            student_to_model_name.get(student).add(model.getName());
        }
    }

    public void tick() {
        ticks_left --;
    }

    public int getTicksLeft () {
        return ticks_left;
    }

    public ConcurrentHashMap<Student,LinkedList<String>> getModels () {
        return student_to_model_name;
    }
}
