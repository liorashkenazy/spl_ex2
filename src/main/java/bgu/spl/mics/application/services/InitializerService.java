package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.InitializeBroadcast;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.objects.*;

import java.util.LinkedList;

public class InitializerService extends MicroService {

    private int initialize_counter = 0;
    private int number_of_objects;
    private ConfigInformation conf;
    private LinkedList<Thread> thread_list = new LinkedList<>();

    public InitializerService(ConfigInformation config_info) {
        super("Initializer service");
        this.conf = config_info;
        number_of_objects = conf.getGpuArray().length + conf.getCpuArray().length + conf.getConferenceArray().length;
    }

    @Override
    protected void initialize() {
        // First subscribe to TerminationBroadcast and InitializationCallback
        subscribeBroadcast(TerminateBroadcast.class, new TerminateCallback());
        subscribeBroadcast(InitializeBroadcast.class, new InitializeCallback());

        for (int i = 0; i < conf.getGpuArray().length; i++) {
            Thread gpu_thread = new Thread(new GPUService("GpuService" + i, conf.getGpuArray()[i]));
            thread_list.add(gpu_thread);
            gpu_thread.start();
        }
        for (int i = 0; i < conf.getCpuArray().length; i++) {
            Thread cpu_thread = new Thread(new CPUService("CpuService" + i, conf.getCpuArray()[i]));
            thread_list.add(cpu_thread);
            cpu_thread.start();
        }
        // Set the cluster to work with our GPUs and CPUs
        Cluster.getInstance().setGPUs(conf.getGpuArray());
        Cluster.getInstance().setCPUs(conf.getCpuArray());
        for (int i = 0; i < conf.getConferenceArray().length; i++) {
            Thread conference_thread = new Thread(new ConferenceService("ConferenceService" + i,
                                                                        conf.getConferenceArray()[i]));
            thread_list.add(conference_thread);
            conference_thread.start();
        }
    }

    private class InitializeCallback implements Callback<InitializeBroadcast> {

        public void call(InitializeBroadcast initializeBroadcast) {
            initialize_counter++;
            // If all the gpus, cpus and conference are initialized, start the students (thereby preventing missed train
            // model events)
            if (initialize_counter == number_of_objects) {
                for(int i = 0; i < conf.getStudentArray().length; i++){
                    StudentService student_service = new StudentService("StudentService" + i,
                                                                        conf.getStudentArray()[i]);
                    Thread student_thread = new Thread(student_service);
                    thread_list.add(student_thread);
                    student_thread.start();
                }
            }
            // If all the gpus, cpus, conference and student are initialized
            if (initialize_counter == number_of_objects + conf.getStudentArray().length) {
                Thread time_thread = new Thread(new TimeService(conf.getTickTime(), conf.getDuration()));
                time_thread.start();
            }
        }
    }

    private class TerminateCallback implements Callback<TerminateBroadcast> {

        public void call(TerminateBroadcast terminateBroadcast) {
            // Waiting for all the micro-service's thread to terminate
            for (Thread t : thread_list) {
                try {
                    t.join();
                } catch (InterruptedException e) { }
            }
            terminate();
        }
    }
}
