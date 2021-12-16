package bgu.spl.mics.application.objects;

import bgu.spl.mics.Callback;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU implements Comparable<GPU> {

    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private int total_gpu_time;
    private Model model;
    private Cluster cluster;
    private int next_data_index_to_process;
    private int current_batch_ticks_left;
    private int data_batches_left_to_train;
    private Runnable train_model_finished_cb;
    private Integer next_expected_idle_time;
    private AtomicInteger vram_train_queue;

    public GPU(Type type, Cluster cluster) {
        this.type = type;
        this.total_gpu_time = 0;
        this.model = null;
        this.cluster = cluster;
        this.train_model_finished_cb = null;
    }

    /**
     * Notifies the GPU that the {@link DataBatch} {@code batch} is now processed, and is ready to be trained
     * <p>
     * @PRE: getBatchTrainQueueLength() < getMaxProcessedBatches()
     * @POST: @PRE(getBatchTrainQueueLength()) + 1 == getBatchTrainQueueLength()
     */
    public boolean batchProcessed(DataBatch batch) {
        int total = vram_train_queue.getAndIncrement();
        if (total > getMaxProcessedBatches()) {
            vram_train_queue.decrementAndGet();
            return false;
        }
        if (total == 1) {
            // If this is the first batch, signal that we can start training
            current_batch_ticks_left = getTicksForBatch();
        }
        return true;
    }

    /**
     * Updates the data structure upon each tick, if the current tick resulted in the completion of a {@link DataBatch}
     * training process, moves on to the next batch, and send another batch to the {@link Cluster} to be processed.
     * If That was the last batch, calls the completion callback.
     * <p>
     * @return [int] Total GPU time used
     * @POST: if (current_batch_ticks_left > 0):
     *          current_batch_ticks_left = @PRE(current_batch_ticks_left) - 1
     * @POST: if (current_batch_ticks_left > 0):
     *          getTotalGPUTime() == @PRE(GetTotalGPUTime()) + 1
     * @POST: if (@PRE(current_batch_ticks_left == 1) && @PRE(GetNextBatchToProcess()) != null):
     *          next_data_index_to_process == @PRE(next_data_index_to_process) + 1000
     *          current_batch_ticks_left == getTicksForBatch()
     *          getModel().getStatus() == Trained
     *
     */
    public void tick()
    {
        if (getModel() == null) {
            return;
        }
        if (current_batch_ticks_left != 0) {
            synchronized (next_expected_idle_time) {
                next_expected_idle_time--;
            }
            current_batch_ticks_left--;
            if (current_batch_ticks_left == 0) {
                // Moving to the next batch to train
                if (vram_train_queue.decrementAndGet() != 0) {
                    current_batch_ticks_left = getTicksForBatch();
                    cluster.trainBatchFinished(this);
                }
                data_batches_left_to_train--;
                if (data_batches_left_to_train == 0) {
                    train_model_finished_cb.run();
                }
            }
            total_gpu_time++;
        }
    }

    /**
     * Return the total GPU time used
     * <p>
     * @return [int] Total GPU time used
     * @INV getTotalGPUTime() >= 0
     * @POST: @PRE(GetTotalGPUTime()) == getTotalGPUTime()
     */
    public int getTotalGPUTime() { return total_gpu_time; }

    /**
     * Starts training a {@link Model} , {@code model}, this starts dividing the {@link Data} into
     * {@link DataBatch} and sends it to the {@link Cluster} for processing. Once training is complete, it will call the
     * {@link Callback} {@code train_model_finish_cb}
     * <p>
     * @param model                     The model to be trained
     * @param train_model_finish_cb     The completion callback
     * @POST: getModel() == model;
     * @POST: model.getStatus() == Model.Status.Training
     * @POST: getNextBatchToProcess().getStartIndex() == 1000 * getMaxProcessedBatches()
     */
    public void trainModel(Model model, Runnable train_model_finish_cb) {
        this.model = model;
        this.next_data_index_to_process = 0;
        model.setStatus(Model.Status.Training);
        this.train_model_finished_cb = train_model_finish_cb;
        data_batches_left_to_train = model.getData().getSize() / 1000;
    }

    /**
     * returns the next {@link DataBatch} that should be sent for processing
     * <p>
     * @PRE: getModel() != null
     * @PRE: next_data_index != getModel().getData().getSize()
     * @POST: @PRE(getNextBatch()) == getNextBatch();
     */
    public DataBatch getNextBatchToProcess() {
        if (hasDataBatchToProcess()) {
            return new DataBatch(getModel().getData(), next_data_index_to_process, this);
        }
        return null;
    }

    public DataBatch sendNextBatchToProcess() {
        DataBatch db = getNextBatchToProcess();
        if (db != null) {
            next_data_index_to_process += 1000;
        }
        return db;
    }

    public boolean hasDataBatchToProcess() {
        return getModel() != null && next_data_index_to_process < getModel().getData().getSize();
    }

    /**
     * Return the current model that is being trained
     * <p>
     * @return The model currently in training
     */
    public Model getModel() { return this.model; }

    /**
     * Tests a deep learning model, and returns the result
     * <p>
     * @return [Boolean] True if the model is good, false otherwise
     */
    public boolean testModel(Model model) { return false; }

    /**
     * Returns the number of {@link DataBatch} this GPU can store in the VRAM
     * <p>
     * @return [int] The maximum number of batches this GPU can hold in the VRAM
     */
    public int getMaxProcessedBatches() {
        switch (type) {
            case GTX1080:
                return 8;
            case RTX2080:
                return 16;
            default:
                return 32;
        }
    }

    /**
     * Returns the number of {@link DataBatch} currently in the VRAM waiting to be trained.
     * <p>
     * @return [int] The number of data batches in the queue
     */
    public int getBatchTrainQueueLength() { return vram_train_queue.get(); }

    /**
     * Returns the number of ticks it takes to train a batch for this GPU
     * <p>
     * @return [int] The number of ticks
     * @INV getTicksForBatch() >= 0;
     */
    public int getTicksForBatch() { return 0; }

    public void addArrivalTime(int time) {
        synchronized (next_expected_idle_time) {
            if (time > next_expected_idle_time) {
                next_expected_idle_time = time + getTicksForBatch();
            }
            else {
                next_expected_idle_time += getTicksForBatch();
            }
        }
    }

    @Override
    public int compareTo(GPU gpu) {
        if (next_expected_idle_time == gpu.next_expected_idle_time) {
            // Sort of a SJF
            return (data_batches_left_to_train * getTicksForBatch()) - (gpu.data_batches_left_to_train * gpu.getTicksForBatch());
        }
        return next_expected_idle_time - gpu.next_expected_idle_time;
    }
}
