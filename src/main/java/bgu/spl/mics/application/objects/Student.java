package bgu.spl.mics.application.objects;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }
    // TODO: name was int type
    private String name;
    private String department;
    private Degree status;
    private Model[] modelsArray;
    private int publications;
    private int papersRead;

    public Student() {}

}
