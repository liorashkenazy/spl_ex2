package bgu.spl.mics.application.objects;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class GPUTest {

    private static Cluster cluster = Cluster.getInstance();
    private static boolean cb_called = false;
    private static GPU[] gpu;
    private static CPU[] cpu;
    private static Model model1;
    private static Model model2;
    private static Student student;

    private static void setCBCalled(boolean b) {
        cb_called = b;
    }

    @Before
    public void setUp() {
        gpu = new GPU[]{new GPU("GTX1080"), new GPU("RTX2080")};
        cpu = new CPU[]{new CPU(32)};
        cluster.setGPUs(gpu);
        cluster.setCPUs(cpu);
        student = new Student("student", "Computer science", "PhD", new Model[]{model1,model2});
        model1 = new Model("model1", "Text", 2000);
        model1.setStudent(student);
        model2 = new Model("model1", "Text", 10000);
        model2.setStudent(student);
    }

    @Test
    public void batchProcessed() {
        Data dt = new Data("Tabular", 1000);
        DataBatch db1 = new DataBatch(dt, 0, gpu[0]);

        assertFalse("Gpu has no model, should return false", gpu[0].batchProcessed(db1));

        gpu[0].trainModel(model1, null);
        gpu[0].batchProcessed(db1);
        assertEquals("Data batch should be added to queue of processed data batches"
                , 1, gpu[0].getBatchTrainQueueLength());
        assertEquals("Gpu should train the new data batch", 4
                , gpu[0].getCurrentBatchTicksLeft().get());

        // Gpu get another 7 dataBatch to train - VRAM is now full
        for (int i = 0; i < 7; i++) {
            gpu[0].batchProcessed(new DataBatch(dt, (i + 1) * 1000, gpu[0]));
        }

        int train_queue_length = gpu[0].getBatchTrainQueueLength();
        assertFalse("VRAM is full - should return false", gpu[0].batchProcessed(db1));
        assertEquals("VRAM is full - shouldn't add new data batch", train_queue_length
                , gpu[0].getBatchTrainQueueLength());
    }

    @Test
    public void idleTick() {
        assertEquals("Initial total GPU time incorrect", 0, gpu[0].getTotalGPUTime());
        gpu[0].tick();
        assertEquals("GPU time increased while not really working", 0, gpu[0].getTotalGPUTime());

        //Gpu has no processed dataBatches to train
        gpu[0].trainModel(model1,null);
        gpu[0].tick();
        assertEquals("GPU time increased while not really working", 0, gpu[0].getTotalGPUTime());
    }

    @Test
    public void tickProcessingDataBatch() {
        gpu[1].trainModel(model1,(models1) -> setCBCalled(true));
        DataBatch db1 = new DataBatch(model1.getData(), 0, gpu[1]);
        DataBatch db2 = new DataBatch(model1.getData(), 1000, gpu[1]);
        cluster.dataBatchProcessed(db1);
        cluster.dataBatchProcessed(db2);

        // Gpu has 2 dataBatch to train
        gpu[1].tick();
        assertEquals("Incorrect total tick count after getting batch to train", 1, gpu[1].getTotalGPUTime());
        assertEquals("Incorrect ticks left for batch after getting batch to train"
                , gpu[1].getTicksForBatch() - 1, gpu[1].getCurrentBatchTicksLeft().get());
        assertEquals("Training queue should increase", 2, gpu[1].getBatchTrainQueueLength());

        gpu[1].tick();

        // After 2 ticks, gpu should finish training the first dataBatch
        assertEquals("Incorrect ticks left for batch after finish training first dataBatch"
                , gpu[1].getTicksForBatch(), gpu[1].getCurrentBatchTicksLeft().get());
        assertEquals("Incorrect count of dataBatch left to train after finish first dataBatch"
                ,model1.getData().getSize()/1000 - 1, gpu[1].getDataBatchesLeftToTrain());
        assertEquals("Finish one batch - training queue should decrease", 1, gpu[1].getBatchTrainQueueLength());

        // Gpu gets another 16 dataBatch to train - VRAM is now full and cluster stores one dataBatch
        for(int i=0; i<16; i++) {
            cluster.dataBatchProcessed(new DataBatch(model1.getData(), 2000 + i*1000, gpu[1]));
        }

        int pre_train_queue = gpu[1].getBatchTrainQueueLength();
        gpu[1].tick();
        gpu[1].tick();

        // After 2 ticks, gpu should finish training the second dataBatch and get another one from cluster
        assertEquals("Gpu should get one dataBatch from cluster", pre_train_queue
                , gpu[1].getBatchTrainQueueLength());
    }

    @Test
    public void tickFinishTrainingModel() {
        gpu[1].trainModel(model1,(model1) -> setCBCalled(true));
        cluster.dataBatchProcessed(new DataBatch(model1.getData(), 0, gpu[1]));
        cluster.dataBatchProcessed(new DataBatch(model1.getData(), 1000, gpu[1]));

        // After 4 ticks, Gpu should finish training the model
        for(int i=0; i<4; i++){
            gpu[1].tick();
        }
        assertEquals("Model should be trained", Model.Status.Trained, gpu[1].getModel().getStatus());
        // Verify the callback was called
        assertTrue("Callback should be called", cb_called);
    }

    @Test
    public void trainModel() {
        gpu[0].trainModel(model1, null);
        assertNotNull("Model should not be null", gpu[0].getModel());
        assertEquals("Model is not properly set", model1, gpu[0].getModel());
        assertEquals("Model status is not properly set", Model.Status.Training, gpu[0].getModel().getStatus());
        assertNotNull("Next data batch should not be null", gpu[0].getNextBatchToProcess());
        assertEquals("Next data batch start index is not properly set", 0
                , gpu[0].getNextBatchToProcess().getStartIndex());
        assertTrue("Cluster should know that gpu is currently training model"
                , cluster.isGpuTrainingModel(gpu[0]));

        // Gpu gets second model to train
        gpu[0].trainModel(model2, null);
        assertEquals("Second model is not properly set", model2, gpu[0].getModel());
        assertEquals("Second model status is not properly set"
                , Model.Status.Training, gpu[0].getModel().getStatus());
    }

    @Test
    public void getNextBatchToProcess() {
        assertNull("Next data batch should be null", gpu[0].getNextBatchToProcess());
        gpu[0].trainModel(model1, null);
        assertNotNull("Next data batch should not be null", gpu[0].getNextBatchToProcess());
    }

    @Test
    public void sendNextBatchToProcess() {
        assertNull("No model - next data batch should be null", gpu[0].sendNextBatchToProcess());
        gpu[0].trainModel(model1, null);
        int next_index = gpu[0].getNextBatchToProcess().getStartIndex();
        DataBatch db = gpu[0].sendNextBatchToProcess();
        assertNotNull("Gpu should send data batch to process", db);
        assertEquals("Next index to process is not properly set "
                , next_index + 1000, gpu[0].getNextBatchToProcess().getStartIndex());
    }

    @Test
    public void hasDataBatchToProcess() {
        assertFalse("Gpu has no model, should return false", gpu[0].hasDataBatchToProcess());

        gpu[0].trainModel(model1, null);
        assertTrue("Gpu didn't send all data to process, should return true", gpu[0].hasDataBatchToProcess());

        gpu[0].sendNextBatchToProcess();
        gpu[0].sendNextBatchToProcess();
        assertFalse("Gpu sent all data to process, should return false", gpu[0].hasDataBatchToProcess());
    }

    @Test
    public void testModel() {
        gpu[0].testModel(model1);
        assertEquals("After test model1's status should be 'Tested'", "Tested", model1.getStatus().toString());
        assertNotEquals("After test model1's result should be 'Good' or 'Bad'", "None",
                model1.getResult().toString());
    }

    @Test
    public void getMaxProcessedBatches() {
        GPU gp1 = new GPU("RTX3090");
        assertEquals("Incorrect VRAM limitation for RTX3090 type ", 32, gp1.getMaxProcessedBatches());

        GPU gp2 = new GPU("RTX2080");
        assertEquals("Incorrect VRAM limitation for RTX2080 type ", 16, gp2.getMaxProcessedBatches());

        GPU gp3 = new GPU("GTX1080");
        assertEquals("Incorrect VRAM limitation for GTX1080 type ", 8, gp3.getMaxProcessedBatches());
    }

    @Test
    public void addArrivalTime() {
        gpu[0].trainModel(model1, null);
        cluster.getNextBatchToProcess(cpu[0]);
        int pre_expected_idle_time = gpu[0].getNextExpectedIdleTime();

        gpu[0].addArrivalTime(1);
        assertEquals("Time wasn't added successfully when added time is smaller than idle time"
                , pre_expected_idle_time + gpu[0].getTicksForBatch(), gpu[0].getNextExpectedIdleTime());

        gpu[1].trainModel(model1, null);
        gpu[1].addArrivalTime(20);
        assertEquals("Time wasn't added successfully when added time is bigger than idle time",
                20 + gpu[1].getTicksForBatch(), gpu[1].getNextExpectedIdleTime());
    }

    @Test
    public void compareTo() {
        // Gpus next expected idle time are equals
        gpu[0].trainModel(model1, null);
        gpu[1].trainModel(model2, null);
        assertTrue("Incorrect compare when idle time equals", gpu[0].compareTo(gpu[1]) < 0);

        // Gpus next expected idle time are not equals, first gpu has more dataBatch left to process
        cluster.getNextBatchToProcess(cpu[0]);
        assertTrue("Incorrect compare when idle time not equals", gpu[0].compareTo(gpu[1]) > 0);
    }
}
