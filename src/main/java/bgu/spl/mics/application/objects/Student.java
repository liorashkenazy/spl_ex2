package bgu.spl.mics.application.objects;

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
    private Model[] models;
    private int current_model;

    public Student(String name, String department, Degree deg, Model[] models) {
        this.name = name;
        this.department = department;
        this.status = deg;
        this.models = models;
        this.publications = 0;
        this.papersRead = 0;
        this.current_model = 0;
    }

    public Degree getDegree() { return status; }

    public void modelFinished(Model model) {
        current_model++;
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
}
