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

	private Cluster(GPU[] gpus, CPU[] cpus) {
		gpu_awaiting_batches = new HashMap<GPU, ConcurrentLinkedQueue<DataBatch>>();
		for (GPU gp : gpus) {
			gpu_awaiting_batches.put(gp, new ConcurrentLinkedQueue<DataBatch>());
		}
	}

	public void trainModel(GPU gp) {
		synchronized (active_gpus_queue) {
			active_gpus_queue.add(gp);
		}
	}

	public void trainBatchFinished(GPU gp) {
		DataBatch db = gpu_awaiting_batches.get(gp).poll();
		if (!gp.batchProcessed(db)) {
			gpu_awaiting_batches.get(gp).add(db);
		}
	}

	public void dataBatchProcessed(DataBatch db) {
		if (!db.getGPU().batchProcessed(db)) {
			gpu_awaiting_batches.get(db.getGPU()).add(db);
		}
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
				active_gpus_queue.add(gpu);
				gpu.addArrivalTime(cpu.getTickCountForDataType(db.getData().getType()));
			}
		}
		return db;
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
