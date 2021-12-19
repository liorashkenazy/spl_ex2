package bgu.spl.mics.application.objects;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	static Cluster instance;

	private PriorityQueue<GPU> active_gpus_queue;
	private HashMap<GPU, ConcurrentLinkedQueue<DataBatch>> gpu_awaiting_batches;
	private GPU[] gpus;
	private CPU[] cpus;
	private Statistics stats;

	private Cluster() {
		gpu_awaiting_batches = new HashMap<GPU, ConcurrentLinkedQueue<DataBatch>>();
		active_gpus_queue = new PriorityQueue<GPU>();
		stats = new Statistics();
	}

	public void setGPUs(GPU[] gpus) {
		this.gpus = gpus;
		for (GPU gp : gpus) {
			gpu_awaiting_batches.put(gp, new ConcurrentLinkedQueue<DataBatch>());
		}
	}

	public void setCPUs(CPU[] cpus) {
		this.cpus = cpus;
	}

	public void trainModel(GPU gp) {
		synchronized (active_gpus_queue) {
			active_gpus_queue.add(gp);
		}
	}

	public void trainBatchFinished(GPU gp) {
		DataBatch db = gpu_awaiting_batches.get(gp).poll();
		if (db != null && !gp.batchProcessed(db)) {
			gpu_awaiting_batches.get(gp).add(db);
		}
	}

	public void dataBatchProcessed(DataBatch db) {
		if (!db.getGPU().batchProcessed(db)) {
			gpu_awaiting_batches.get(db.getGPU()).add(db);
		}
		stats.dataBatchProcessed();
	}

	public void modelTrainFinished(Model m) {
		stats.addModel(m.getName());
	}

	public DataBatch getNextBatchToProcess(CPU cpu) {
		DataBatch db = null;
		synchronized (active_gpus_queue) {
			GPU gpu = active_gpus_queue.poll();
			if (gpu == null) {
				return null;
			}
			db = gpu.sendNextBatchToProcess();
			if (gpu.hasDataBatchToProcess()) {
				gpu.addArrivalTime(cpu.getTickCountForDataType(db.getData().getType()));
				active_gpus_queue.add(gpu);
			}
		}
		return db;
	}

	public boolean isGpuTrainingModel(GPU gpu) {
		return active_gpus_queue.contains(gpu);
	}

	public void summarize() {
		for (GPU gp : gpus) {
			stats.addGPUTime(gp.getTotalGPUTime());
		}
		for (CPU cp: cpus) {
			stats.addCPUTime(cp.getTotalCPUTime());
		}
	}

	public Statistics getStats() {
		return stats;
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		private static Cluster instance = new Cluster();
	}
}
