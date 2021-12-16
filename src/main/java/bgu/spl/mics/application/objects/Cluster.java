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

	private PriorityQueue<GPU> data_send_queue;

	private Cluster() { }

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return SingeltonHolder.instance;
	}

	private static class SingeltonHolder {
		private static Cluster instance = new Cluster();
	}
}
