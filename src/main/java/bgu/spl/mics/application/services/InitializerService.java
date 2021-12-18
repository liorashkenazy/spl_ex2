package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.InitializeBroadcast;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.objects.*;

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
    private LinkedList<StudentService> student_service_list = new LinkedList<>();

    public InitializerService(ConfigInformation config_info) {
        super("Initializer service");
        this.student_array = config_info.getStudentArray();
        this.gpu_array = config_info.getGpuArray();
        this.cpu_array = config_info.getCpuArray();
        this.conference_array = config_info.getConferenceArray();
        this.tick_time = config_info.getTickTime();
        this.duration = config_info.getDuration();
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
        // Set the cluster to work with our GPUs and CPUs
        Cluster.getInstance().setGPUs(gpu_array);
        Cluster.getInstance().setCPUs(cpu_array);
        for (int i = 0; i < conference_array.length; i++) {
            Thread conference_thread = new Thread(new ConferenceService("ConferenceService"+i, conference_array[i]));
            thread_list.add(conference_thread);
            conference_thread.start();
        }
    }

    private class InitializeCallback implements Callback<InitializeBroadcast> {

        public void call(InitializeBroadcast initializeBroadcast) {
            initialize_counter++;
            // If all the gpus, cpus and conference are initialized
            if (initialize_counter == number_of_objects) {
                for(int i=0; i<student_array.length; i++){
                    StudentService student_service = new StudentService("StudentService"+i, student_array[i]);
                    Thread student_thread = new Thread(student_service);
                    thread_list.add(student_thread);
                    student_service_list.add(student_service);
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

    private class TerminateCallback implements Callback<TerminateBroadcast> {

        public void call(TerminateBroadcast terminateBroadcast) {
            // Waiting for all the micro-service's thread to terminate
            for (int i = 0; i < thread_list.size(); i++) {
                // If this is student thread it may be waiting for a test result, interrupt it in that case
                if (i >= number_of_objects && i < number_of_objects + student_array.length) {
                    if (student_service_list.get(i - number_of_objects).isWaitingForResult()) {
                        thread_list.get(i).interrupt();
                    }
                }
                try {
                    thread_list.get(i).join();
                } catch (InterruptedException e){}
            }
            terminate();
        }
    }
}
