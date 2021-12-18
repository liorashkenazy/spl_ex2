package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    enum Status {PreTrained, Training, Trained, Tested}
    enum Result {None, Good, Bad}

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private Result result;
    private boolean published;

    public Model(String name, String type, int size) {
        this.name = name;
        this.data = new Data(type,size);
        this.student = null;
        this.status = Status.PreTrained;
        this.result = Result.None;
        this.published = false;
    }

    public void setStatus(Status status) { this.status = status; }

    public void setStudent(Student student) { this.student = student; }

    public String getName() { return name; }

    public Data getData() { return data; }

    public Student getStudent() { return student; }

    public Status getStatus() { return status; }

    public Result getResult() {return result; }

    public void setResult(Result res) { result = res; }

    public boolean isResultGood() { return result.equals(Result.Good); }

    public boolean isPublished() { return published; }

    public void setPublished(boolean published) { this.published = published; }

    public String toString() {
        return "name: " + name + ", student name: " + student.getName() + ", status: " + status + ", result: " + result + "\n";
    }
}
