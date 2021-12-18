package bgu.spl.mics.application.objects;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private int cores;
    private DataBatch data;
    private final Cluster cluster;
    private int ticks_left;
    private int total_cpu_time;
    private final int base_process_ticks;

    /**
     * Constructs a CPU with the specified cluster and cores
     * <p>
     * @param cores   The number of cores this CPU has
     */
    public CPU(int cores)
    {
        this.cores = cores;
        this.data = null;
        this.ticks_left = 0;
        this.total_cpu_time = 0;
        this.cluster = Cluster.getInstance();
        this.base_process_ticks = 32 / cores;
    }

    /**
     * This function should be called every time a tick occurs. Once enough ticks have passed to finish processing the
     * {@link DataBatch}, this function will notify the {@link Cluster} that processing is complete. If no data is being
     * processed at the moment, request data to process from the Cluster.
     *
     * <p>
     * @POST: if(@PRE: getData() != null)
     *              @PRE(getTicksLeftForBatch()) - 1 == getTicksLeftForBatch()
     * @POST: if(@PRE: getData() != null)
     *              @PRE(getTotalCPUTime()) + 1 == getTotalCPUTime()
     * @POST: if ((@PRE: getTicksLeftForBatch() == 1):
     *              getData() != @PRE: getData();
     * @POST: if ((@PRE: getTicksLeftForBatch() == 1):
     *              if (getData() != null):
     *                  getTicksLeftForBatch() = getTickCountForDataType(data.getData().getType())
     *
     */
    public void tick() {
        if (data != null) {
            ticks_left--;
            total_cpu_time++;
            if (ticks_left == 0) {
                cluster.dataBatchProcessed(data);
                this.data = null;
            }
        }

        if (data == null) {
            data = cluster.getNextBatchToProcess(this);
            if (data != null) {
                this.ticks_left = getTickCountForDataType(data.getData().getType());
            }
        }
    }

    /**
     * returns the number of ticks left to process the current {@link DataBatch}
     * <p>
     * @return [int] The number of remaining ticks to finish processing the current data
     * @INV getTicksLeftForBatch() >= 0;
     * @POST: @PRE(getTicksLeftForBatch()) == getTicksLeftForBatch()
     */
    public int getTicksLeftForBatch() { return ticks_left; }

    /**
     * Return the current data that is being processed
     * <p>
     * @return The current {@link DataBatch} being processed
     * @POST: @PRE(getData()) == getData()()
     */
    public DataBatch getData() { return data; }

    /**
     * Return the total CPU time used
     * <p>
     * @return [int] Total CPU time used
     * @INV getTotalCPUTime() >= 0
     * @POST: @PRE(getTotalCPUTime()) == getTotalCPUTime()
     */
    public int getTotalCPUTime() { return total_cpu_time; }

    /**
     * Check if the {@link DataBatch} {@code batch} is in the CPU's processing queue
     * <p>
     * @return [boolean] True if the batch is in the processing queue
     */
    public boolean isDataInProcessing(DataBatch batch) { return data == batch; }

    /**
     * Calculate the number of ticks required to process {@link DataBatch} of {@link Data.Type} {@code type}
     * <p>
     * @return [int] The number of ticks required for this processor to process the data
     * @POST: if (type = "Tabular")
     *          getTickCountForDataType() = getBaseProcessTicks() * 1
     * @POST: if (type = "Text")
     *          getTickCountForDataType() = getBaseProcessTicks() * 2
     * @POST: if (type = "Images")
     *          getTickCountForDataType() = getBaseProcessTicks() * 4
     */
    public int getTickCountForDataType(Data.Type type) {
        return base_process_ticks * (type == Data.Type.Tabular ? 1 : type == Data.Type.Images ? 4 : 2);
    }

    public int getBaseProcessTicks() { return base_process_ticks; }

    public String toString() {
        return "cores: " + cores + "\n";
    }
}
