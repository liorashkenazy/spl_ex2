package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.InitializeBroadcast;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Student;

import java.util.LinkedList;

public class InitializerService extends MicroService {

    private Student[] student_array;
    private GPU[] gpu_array;
    private CPU[] cpu_array;
    private ConferenceInformation[] conference_array;
    private int tick_time;
    private int duration;
    private int initialize_counter = 0;
    private int number_of_objects;
    private LinkedList<Thread> thread_list = new LinkedList<>();

    public InitializerService(Student[] student_array, GPU[] gpu_array, CPU[] cpu_array,
                              ConferenceInformation[] conference_array, int tick_time, int duration) {
        super("Initializer service");
        this.student_array = student_array;
        this.gpu_array = gpu_array;
        this.cpu_array = cpu_array;
        this.conference_array = conference_array;
        this.tick_time = tick_time;
        this.duration = duration;
        number_of_objects = gpu_array.length + cpu_array.length + conference_array.length;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateBroadcast.class, new TerminateCallback());
        subscribeBroadcast(InitializeBroadcast.class, new InitializeCallback());
        for (int i = 0; i < gpu_array.length; i++) {
            Thread gpu_thread = new Thread(new GPUService("GpuService"+i, gpu_array[i]));
            thread_list.add(gpu_thread);
            gpu_thread.start();
        }
        for (int i = 0; i < cpu_array.length; i++) {
            Thread cpu_thread = new Thread(new CPUService("CpuService"+i, cpu_array[i]));
            thread_list.add(cpu_thread);
            cpu_thread.start();
        }
        for (int i = 0; i < conference_array.length; i++) {
            Thread conference_thread = new Thread(new ConferenceService("ConferenceService"+i, conference_array[i]));
            thread_list.add(conference_thread);
            conference_thread.start();
        }
    }

    public class InitializeCallback implements Callback<InitializeBroadcast> {

        public void call(InitializeBroadcast initializeBroadcast) {
            initialize_counter++;
            // If all the gpus, cpus and conference are initialized
            if (initialize_counter == number_of_objects) {
                for(int i=0; i<student_array.length; i++){
                Thread student_thread = new Thread(new StudentService("StudentService"+i, student_array[i]));
                student_thread.start();
                }
            }
            // If all the gpus, cpus, conference and student are initialized
            if (initialize_counter == number_of_objects + student_array.length) {
                Thread time_thread = new Thread(new TimeService(tick_time,duration));
                time_thread.start();
            }
        }
    }

    public class TerminateCallback implements Callback<TerminateBroadcast> {

        public void call(TerminateBroadcast terminateBroadcast) {
            // Waiting for all the micro-service's thread to terminate
            for (Thread thread : thread_list) {
                try {
                    thread.join();
                } catch (InterruptedException e){}
            }
            terminate();
            // TODO: implement statistics output
        }
    }
}
