package bgu.spl.mics.application.objects;

import java.util.PriorityQueue;

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

	private Cluster() { }

	public void trainModel(GPU gp) {
		synchronized (active_gpus_queue) {
			active_gpus_queue.add(gp);
		}
	}

	public void dataBatchProcessed(DataBatch db) {
		db.getGPU().batchProcessed(db);
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
