package bgu.spl.mics.application.objects;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private int cores;
    private DataBatch data;
    private Cluster cluster;
    private int ticks_left;
    private int total_cpu_time;

    /**
     * Constructs a CPU with the specified cluster and cores
     * <p>
     * @param cores   The number of cores this CPU has
     */
    public CPU(int cores)
    {
        this.cores = cores;
        this.cluster = Cluster.getInstance();
        this.data = null;
        this.ticks_left = 0;
        this.total_cpu_time = 0;
    }

    /**
     * This function should be called every time a tick occurs. Once enough ticks have passed to finish processing the
     * {@link DataBatch}, this function will notify the {@link Cluster} that processing is complete.
     * <p>
     * @PRE: getData() != null;
     * @POST: @PRE(getTicksLeftForBatch()) - 1 == getTicksLeftForBatch()
     * @POST: if (getTicksLeftForBatch() == 0):
     *          getData() != @PRE: getData();
     * @POST: if (getData() != null):
     *           @PRE(getTotalCPUTime()) + 1 == getTotalCPUTime()
     */
    public void tick() {
        if (data != null) {
            ticks_left--;
            if (ticks_left == 0) {
                // do something
                data = null;
            }
            total_cpu_time++;
        }
    }

    /**
     * returns the number of ticks left to process the current {@link DataBatch}
     * <p>
     * @return [int] The number of remaining ticks to finish processing the current data
     * @PRE: getData() != null;
     * @INV getTicksLeftForBatch() >= 0;
     */
    public int getTicksLeftForBatch() { return ticks_left; }

    /**
     * Start processing new data
     * <p>
     * @return The current {@link DataBatch} being processed
     * @POST: isDataInProcessing(data) == true
     * @POST: if (@PRE(getData() == null):
     *          getData() == data
     * @POST: if (@PRE(getData()) == null):
     *          if (data.getType() == Tabular:
     *              getTicksLeftForBatch() == 32 / cores
     *          if (data.getType() == Text:
     *              getTicksLeftForBatch() == (32 / cores) * 2
     *          if (data.getType() == Image:
     *              getTicksLeftForBatch() == (32 / cores) * 4
     */
    public void addDataForProcessing(DataBatch data) {
        this.data = data;
        ticks_left = (32 / cores);
        switch (this.data.getData().getType()) {
            case Text:
                ticks_left *= 2;
                break;
            case Images:
                ticks_left *= 4;
                break;
            default:
                break;
        }
    }

    /**
     * Return the current data that is being processed
     * <p>
     * @return The current {@link DataBatch} being processed
     */
    public DataBatch getData() { return data; }

    /**
     * Return the total CPU time used
     * <p>
     * @return [int] Total CPU time used
     * @INV getTotalCPUTime() >= 0
     * @POST: @PRE(GetTotalCPUTime()) == getTotalCPUTime()
     */
    public int getTotalCPUTime() { return total_cpu_time; }

    /**
     * Check if the {@link DataBatch} {@code batch} is in the CPU's processing queue
     * <p>
     * @return [boolean] True if the batch is in the processing queue
     */
    public boolean isDataInProcessing(DataBatch batch) { return data == batch; }

    public String toString() {
        return "cores: " + cores + "\n";
    }
}
