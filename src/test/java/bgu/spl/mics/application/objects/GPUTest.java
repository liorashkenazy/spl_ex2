package bgu.spl.mics.application.objects;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Callback;
import static org.junit.Assert.*;

import bgu.spl.mics.MessageBusImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class GPUTest {

    private static Cluster cluster = Cluster.getInstance();
    private static boolean cb_called;
    private static GPU[] gpu;
    private static CPU[] cpu;
    private static Model model1;
    private static Model model2;

    private static void setCBCalled(boolean b) {
        cb_called = b;
    }

    @Before
    public void setUp() {
        gpu = new GPU[]{new GPU("GTX1080"), new GPU("RTX2080")};
        model1 = new Model("model1", "Text", 2000);
        model2 = new Model("model1", "Text", 10000);
        cpu = new CPU[]{new CPU(32)};
        cluster.setGPUs(gpu);
        cluster.setCPUs(cpu);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void batchProcessed() {

        Data dt = new Data("Tabular", 1000);
        DataBatch db1 = new DataBatch(dt, 0, gpu[0]);
        DataBatch db2 = new DataBatch(dt, 0, gpu[0]);
        DataBatch db3 = new DataBatch(dt, 0, gpu[0]);
        DataBatch db4 = new DataBatch(dt, 0, gpu[0]);

        assertFalse("Gpu has no model1, should return false", gpu[0].batchProcessed(db1));

        gpu[0].trainModel(model1, (model1) -> setCBCalled(true));
        gpu[0].batchProcessed(db1);
        assertEquals("Data batch wasn't added to queue of processed data batches"
                , 1, gpu[0].getBatchTrainQueueLength());
        assertEquals("Gpu should train the new data batch", 4
                , gpu[0].getCurrentBatchTicksLeft().get());

        for (int i = 0; i < 7; i++) {
            gpu[0].batchProcessed(new DataBatch(dt, (i + 1) * 1000, gpu[0]));
        }

        int train_queue_length = gpu[0].getBatchTrainQueueLength();
        gpu[0].batchProcessed(db1);
        assertFalse("VRAM is full - should return false", gpu[0].batchProcessed(db1));
        assertEquals("VRAM is full - shouldn't add new data batch", train_queue_length
                , gpu[0].getBatchTrainQueueLength());
    }

    @Test
    public void tick() {


//        GPU gp = new GPU("RTX3090");
//
//        assertEquals("Initial total GPU time incorrect", 0, gp.getTotalGPUTime());
//        gp.tick();
//        assertEquals("GPU time increased while not really working", 0, gp.getTotalGPUTime());
//        setCBCalled(false);
//        Model model1 = new Model("TestModel",
//                               "Tabular", 2 * 1000);
//        gp.trainModel(model1, (model1) -> setCBCalled(true));
//        gp.tick();
//        assertEquals("GPU time increased while not really working", 0, gp.getTotalGPUTime());
//        gp.batchProcessed(new DataBatch(model1.getData(), 0, gp));
//        assertEquals("Incorrect tick count after batch processed", 1, gp.getTotalGPUTime());
//        assertEquals("Training queue should be size", 1, gp.getBatchTrainQueueLength());
//
//        // We've inserted one batch, and then caused a tick, the batch should be removed now
//        gp.tick();
//        assertEquals("GPU tick count not updated", 1, gp.getTotalGPUTime());
//        assertEquals("Batch not removed from training queue", 0, gp.getBatchTrainQueueLength());
//
//        // Now process the second batch out of 2
//        gp.batchProcessed(new DataBatch(model1.getData(), 1000, gp));
//        gp.tick();
//        assertEquals("GPU tick count not updated", 2, gp.getTotalGPUTime());
//        assertEquals("Model should be trained", Model.Status.Trained, gp.getModel().getStatus());
//        // Verify the callback was called
//        assertTrue("Callback should be called", cb_called);
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
        assertTrue("Cluster should know that gpu is currently training model1"
                , cluster.isGpuTrainingModel(gpu[0]));

        // Scenario 2: a Batch with short data
        Model model2 = new Model("TestModel2", "Tabular", 1000);
        gpu[0].trainModel(model1, null);
        assertEquals("Second model1 is not properly set", model1, gpu[0].getModel());
        assertEquals("Second model1 status is not properly set", Model.Status.Training, gpu[0].getModel().getStatus());
    }

    @Test
    public void getNextBatchToProcess() {
        assertNull("Next data batch should be null", gpu[0].getNextBatchToProcess());
        gpu[0].trainModel(model1, null);
        assertNotNull("Next data batch should not be null", gpu[0].getNextBatchToProcess());
    }

    @Test
    public void sendNextBatchToProcess() {
        assertNull("No model1 - next data batch should be null", gpu[0].sendNextBatchToProcess());
        gpu[0].trainModel(model1, null);
        int next_index = gpu[0].getNextBatchToProcess().getStartIndex();
        DataBatch db = gpu[0].sendNextBatchToProcess();
        assertNotNull("Gpu should send data batch to process", db);
        assertEquals("Next index to process is not properly set "
                , next_index + 1000, gpu[0].getNextBatchToProcess().getStartIndex());
    }

    @Test
    public void hasDataBatchToProcess() {
        assertFalse("Gpu has no model1, should return false", gpu[0].hasDataBatchToProcess());

        gpu[0].trainModel(model1, null);
        assertTrue("Gpu didn't send all data to process, should return true", gpu[0].hasDataBatchToProcess());

        gpu[0].sendNextBatchToProcess();
        gpu[0].sendNextBatchToProcess();
        assertFalse("Gpu sent all data to process, should return false", gpu[0].hasDataBatchToProcess());
    }

    @Test
    public void testModel() {
        Model[] models = {model1};
        Student student = new Student("student1", "Computer Science", "MSc", models);
        model1.setStudent(student);
        gpu[0].testModel(models[0]);
        assertEquals("After test model1's status should be 'Tested'", "Tested", models[0].getStatus().toString());
        assertNotEquals("After test model1's result should be 'Good' or 'Bad'", "None",
                models[0].getResult().toString());
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
        DataBatch db = new DataBatch(new Data("Text", 1000), 0, gpu[0]);
        gpu[0].trainModel(model1, null);
        gpu[0].batchProcessed(db);
        int pre_expected_idle_time = gpu[0].getNextExpectedIdleTime();
        gpu[0].addArrivalTime(1);
        //TODO: idle time
        assertEquals("Time wasn't added successfully when added time is smaller than idle time"
                , pre_expected_idle_time + gpu[0].getTicksForBatch(), gpu[0].getNextExpectedIdleTime());

        gpu[1].trainModel(model1, null);
        assertEquals("Time wasn't added successfully when added time is bigger than idle time",
                0, gpu[1].getNextExpectedIdleTime());
        gpu[1].addArrivalTime(20);
        assertEquals("Time wasn't added successfully when added time is bigger than idle time",
                20 + gpu[1].getTicksForBatch(), gpu[1].getNextExpectedIdleTime());
    }

    @Test
    public void compareTo() {
        gpu[0].trainModel(model1, null);
        gpu[1].trainModel(model2, null);
        assertTrue("Incorrect compare when idle time equals", gpu[0].compareTo(gpu[1]) < 0);

        //TODO: idle time not equals
    }
}
