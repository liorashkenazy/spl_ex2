package bgu.spl.mics.application.objects;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Callback;
import static org.junit.Assert.*;

import bgu.spl.mics.MessageBusImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class GPUTest {

    private static Cluster cluster = Cluster.getInstance();
    private static boolean cb_called;
    private static GPU[] gpu;
    private static CPU[] cpu;
    private static Model model;

    private static void setCBCalled(boolean b) {
        cb_called = b;
    }

    @BeforeClass
    public static void setUp() {
        gpu = new GPU[]{new GPU("GTX1080")};
        model = new Model("model","Text",2000);
        cpu = new CPU[] {new CPU(32)};
        cluster.setGPUs(gpu);
        cluster.setCPUs(cpu);

    }

    @Test
    public void batchProcessed() {

        Data dt = new Data("Tabular",1000);
        DataBatch db1 = new DataBatch(dt, 0, gpu[0]);
        DataBatch db2 = new DataBatch(dt, 0, gpu[0]);
        DataBatch db3 = new DataBatch(dt, 0, gpu[0]);
        DataBatch db4 = new DataBatch(dt, 0, gpu[0]);

        assertFalse("Gpu has no model, should return false", gpu[0].batchProcessed(db1));

        gpu[0].trainModel(model,(model1) -> setCBCalled(true));
        gpu[0].batchProcessed(db1);
        assertEquals("Data batch wasn't added to queue of processed data batches"
                                                ,1 ,gpu[0].getBatchTrainQueueLength());
        assertEquals("Gpu should train the new data batch",new AtomicInteger(4)
                                                , gpu[0].getCurrentBatchTicksLeft());

        for (int i=0; i<7; i++) {
            gpu[0].batchProcessed(new DataBatch(dt, (i+1)*1000, gpu[0]));
        }

        int train_queue_length = gpu[0].getBatchTrainQueueLength();
        assertFalse("VRAM is full - should return false",gpu[0].batchProcessed(db1));
        assertEquals("VRAM is full - shouldn't add new data batch", train_queue_length
                                                , gpu[0].batchProcessed(db1));
    }

    @Test
    public void tick() {






//        GPU gp = new GPU("RTX3090");
//
//        assertEquals("Initial total GPU time incorrect", 0, gp.getTotalGPUTime());
//        gp.tick();
//        assertEquals("GPU time increased while not really working", 0, gp.getTotalGPUTime());
//        setCBCalled(false);
//        Model model = new Model("TestModel",
//                               "Tabular", 2 * 1000);
//        gp.trainModel(model, (model1) -> setCBCalled(true));
//        gp.tick();
//        assertEquals("GPU time increased while not really working", 0, gp.getTotalGPUTime());
//        gp.batchProcessed(new DataBatch(model.getData(), 0, gp));
//        assertEquals("Incorrect tick count after batch processed", 1, gp.getTotalGPUTime());
//        assertEquals("Training queue should be size", 1, gp.getBatchTrainQueueLength());
//
//        // We've inserted one batch, and then caused a tick, the batch should be removed now
//        gp.tick();
//        assertEquals("GPU tick count not updated", 1, gp.getTotalGPUTime());
//        assertEquals("Batch not removed from training queue", 0, gp.getBatchTrainQueueLength());
//
//        // Now process the second batch out of 2
//        gp.batchProcessed(new DataBatch(model.getData(), 1000, gp));
//        gp.tick();
//        assertEquals("GPU tick count not updated", 2, gp.getTotalGPUTime());
//        assertEquals("Model should be trained", Model.Status.Trained, gp.getModel().getStatus());
//        // Verify the callback was called
//        assertTrue("Callback should be called", cb_called);
    }

    @Test
    public void trainModel() {
        GPU gp = new GPU("RTX2080");

        Model model = new Model("TestModel", "Tabular", 32 * 1000);
        gp.trainModel(model, null);
        assertEquals("Model is not properly set", model, gp.getModel());
        assertEquals("Model status is not properly set", Model.Status.Training, gp.getModel().getStatus());
        assertEquals("Next data batch start index is not properly set",
                gp.getMaxProcessedBatches() * 1000,
                gp.getNextBatchToProcess().getStartIndex());

        // Scenario 2: a Batch with short data
        Model model2 = new Model("TestModel2", "Tabular", 1000);
        gp.trainModel(model, null);
        assertEquals("second model is not properly set", model, gp.getModel());
        assertEquals("second model status is not properly set", Model.Status.Training, gp.getModel().getStatus());
        assertNull("Next data batch should be null", gp.getNextBatchToProcess());
    }

    @Test
    public void getNextBatchToProcess() {
        GPU gp = new GPU("RTX2080");

        assertNull("Next data batch should be null", gp.getNextBatchToProcess());
        Model model = new Model("TestModel", "Tabular", 32 * 1000);
        gp.trainModel(model, null);
        assertNotNull("Next data batch should not be null", gp.getNextBatchToProcess());
    }

    @Test
    public void sendNextBatchToProcess() {
        GPU gp = new GPU("RTX3090");

    }

    @Test
    public void hasDataBatchToProcess() {

    }

    @Test
    public void testModel() {
        GPU gp = new GPU("RTX3090");
        Model[] model = {new Model("model 1","Text",2000)};
        Student student = new Student("student1","Computer Science","MSc",model);
        model[0].setStudent(student);
        gp.testModel(model[0]);
        assertEquals("After test model's status should be 'Tested'","Tested",model[0].getStatus().toString());
        assertNotEquals("After test model's status should be 'Good' or 'Bad'","None",
                                                                            model[0].getResult().toString());
    }

    @Test
    public void getMaxProcessedBatches() {
        GPU gp1 = new GPU("RTX3090");
        assertEquals("Incorrect VRAM limitation for RTX3090 type ",32,gp1.getMaxProcessedBatches());

        GPU gp2 = new GPU("RTX2080");
        assertEquals("Incorrect VRAM limitation for RTX2080 type ",16,gp2.getMaxProcessedBatches());

        GPU gp3 = new GPU("GTX1080");
        assertEquals("Incorrect VRAM limitation for GTX1080 type ",8,gp3.getMaxProcessedBatches());
    }

    @Test
    public void addArrivalTime() {
        DummyGPU gpu1 = new DummyGPU("RTX3090");
        gpu1.addArrivalTime(5);
        assertEquals("The time wasn't added successfully when time is smaller than idle time",
                                                                11,gpu1.getNextExpectedIdleTime());

        DummyGPU gpu2 = new DummyGPU("RTX3090");
        gpu1.addArrivalTime(20);
        assertEquals("The time wasn't added successfully when time is bigger than idle time",
                21,gpu2.getNextExpectedIdleTime());
    }

    @Test
    public void compareTo() {

    }

//
//    @Test
//    public void batchProcessed() {
//        GPU gp = new GPU("RTX2080");
//
//        // The queue should not change if there is no model in training atm
//        assertEquals("Batch training queue should be empty", 0, gp.getBatchTrainQueueLength());
//        Model model = new Model("TestModel", "Tabular", 32 * 1000);
//        gp.trainModel(model, null);
//        DataBatch db = new DataBatch(model.getData(), 0, gp);
//        gp.batchProcessed(db);
//        assertEquals("Batch training queue size", 1, gp.getBatchTrainQueueLength());
//    }

    class DummyGPU extends GPU {
        public DummyGPU(String type) {
            super(type);

//            Model model;
//            Cluster cluster;
//            int next_data_index_to_process;
//            AtomicInteger current_batch_ticks_left;
//            int data_batches_left_to_train;
//            Callback<Model> train_model_finished_cb;
//            Integer next_expected_idle_time;
//            AtomicInteger vram_train_queue;
//            final int ticks_to_train;

        }
    }
}
