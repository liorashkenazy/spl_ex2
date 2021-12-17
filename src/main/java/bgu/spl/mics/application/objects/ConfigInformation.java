package bgu.spl.mics.application.objects;

public class ConfigInformation {
    private Student[] student_array;
    private GPU[] gpu_array;
    private CPU[] cpu_array;
    private ConferenceInformation[] conference_array;
    private int tick_time;
    private int duration;

    public ConfigInformation(Student[] student_array, GPU[] gpu_array, CPU[] cpu_array,
                             ConferenceInformation[] conference_array, int tick_time, int duration) {
        this.student_array = student_array;
        this.gpu_array = gpu_array;
        this.cpu_array = cpu_array;
        this.conference_array = conference_array;
        this.tick_time = tick_time;
        this.duration = duration;
    }

    public Student[] getStudentArray() { return student_array; }

    public GPU[] getGpuArray() { return gpu_array; }

    public CPU[] getCpuArray() { return cpu_array; }

    public ConferenceInformation[] getConferenceArray() { return conference_array; }

    public int getTickTime() { return tick_time; }

    public int getDuration () { return duration; }
}
