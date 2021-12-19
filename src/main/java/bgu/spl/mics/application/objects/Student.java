package bgu.spl.mics.application.objects;

import com.google.gson.annotations.Expose;

import java.util.LinkedList;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    @Expose (serialize = false)
    private Model[] models;
    @Expose (deserialize = false)
    private LinkedList<Model> trainedModels;
    @Expose (serialize = false)
    private int current_model;

    public Student(String name, String department, String status, Model[] models) {
        this.name = name;
        this.department = department;
        this.status = Degree.valueOf(status);
        this.models = models;
        this.publications = 0;
        this.papersRead = 0;
        this.current_model = 0;
    }

    public LinkedList<Model> getTrainedModels() {
        if (trainedModels == null) {
            trainedModels = new LinkedList<Model>();
        }
        return trainedModels;
    }

    public String getName() { return name; }

    public Degree getDegree() { return status; }

    public Model[] getModels() { return models; }

    public String getDepartment() { return department; }

    public void modelFinished(Model model) {
        current_model++;
        getTrainedModels().add(model);
    }

    public Model getCurrentModel() {
        if (current_model < models.length) {
            return models[current_model];
        }
        return null;
    }

    public void readPapers(LinkedList<Model> models) {
        for (Model model : models) {
            if (model.getStudent() == this) {
                publications++;
            }
            else {
                papersRead++;
            }
        }
    }

    public int getPapersRead() { return papersRead; }

    public int getPublications() { return publications; }

    // Set the field 'Student' in each of the student's models
    public void setStudentForModels() {
        for (Model model : models) {
            model.setStudent(this);
        }
    }

    public String toString() {
        String to_string = "name: " + name + ", department: " + department + ", degree: " + status;
        to_string += " papers_read: " + getPapersRead() + " papers_published: " + getPublications() + ", models:\n[";
        for (int i=0; i<models.length; i++) {
            to_string = to_string + models[i].toString();
        }
        return to_string + "] \n";
    }
}