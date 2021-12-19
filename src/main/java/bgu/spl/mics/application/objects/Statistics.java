package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {

    private int total_cpu_time;
    private int total_gpu_time;
    private ConcurrentLinkedQueue<String> models_trained;
    private AtomicInteger data_batches_processed;

    public Statistics() {
        this.total_gpu_time = 0;
        this.total_cpu_time = 0;
        this.models_trained = new ConcurrentLinkedQueue<String>();
        this.data_batches_processed = new AtomicInteger(0);
    }

    public void dataBatchProcessed() { this.data_batches_processed.incrementAndGet(); }

    public void addModel(String model_name) { this.models_trained.add(model_name); }

    public void addGPUTime(int time) { this.total_gpu_time += time; }

    public void addCPUTime(int time) { this.total_cpu_time += time; }

    public String toString() {
        String ans = "Total GPU time: " + total_gpu_time;
        ans += "\nTotal CPU time: " + total_cpu_time;
        ans += "\n DataBatches processed: " + data_batches_processed.get();
        ans += "\n Models Trained: [";
        for (String model : models_trained) {
            ans += model + ", ";
        }
        ans += "]";
        return ans;
    }

    public int getTotalCPUTime() { return total_cpu_time; }
    public int getTotalGPUTime() { return total_gpu_time; }
    public int getTotalBatches() { return data_batches_processed.get(); }
}
