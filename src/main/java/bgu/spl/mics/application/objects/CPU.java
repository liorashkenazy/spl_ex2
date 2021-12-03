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

    /**
     * Constructs a CPU with the specified cluster and cores
     * <p>
     * @param cores   The number of cores this CPU has
     * @param cluster The cluster this CPU belongs to
     */
    public CPU(int cores, Cluster cluster)
    {
        this.cores = cores;
        this.cluster = cluster;
        this.data = null;
        this.ticks_left = 0;
    }

    /**
     * This function should be called every time a tick occurs. Once enough ticks have passed to finish processing the
     * {@link DataBatch}, this function will notify the {@link Cluster} that processing is complete.
     * <p>
     * @PRE: getData() != null;
     * @POST: @PRE(getTicksLeftForBatch()) - 1 == getTicksLeftForBatch()
     * @POST: if (getTicksLeftForBatch() == 0):
     *          getData() == null;
     */
    public void tick() {
        if (data != null) {
            ticks_left--;
            if (ticks_left == 0) {
                // do something
                data = null;
            }
        }
    }

    /**
     * returns the number of ticks left to process the current {@link DataBatch}
     * <p>
     * @return [int] The number of remaining ticks to finish processing the current data
     * @PRE: getData() != null;
     * @INV: getTicksLeftForBatch() >= 0;
     */
    public int getTicksLeftForBatch() { return ticks_left; }

    /**
     * Start processing new data
     * <p>
     * @return The current {@link DataBatch} being processed
     * @PRE: getData() != null;
     * @INV: getTicksLeftForBatch() >= 0;
     */
    public void processData(DataBatch data) {
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
}
