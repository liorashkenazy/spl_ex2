package bgu.spl.mics.application.objects;

import bgu.spl.mics.Callback;
import static org.junit.Assert.*;
import org.junit.Test;

public class GPUTest {

    private Cluster cluster = Cluster.getInstance();
    private boolean cb_called;

    private void setCBCalled(boolean b) {
        cb_called = b;
    }

    @Test
    public void tick() {
        GPU gp = new GPU("RTX3090");

        assertEquals("Initial total GPU time incorrect", 0, gp.getTotalGPUTime());
        gp.tick();
        assertEquals("GPU time increased while not really working", 0, gp.getTotalGPUTime());
        setCBCalled(false);
        Model model = new Model("TestModel",
                               "Tabular", 2 * 1000);
        gp.trainModel(model, () -> setCBCalled(true));
        gp.tick();
        assertEquals("GPU time increased while not really working", 0, gp.getTotalGPUTime());
        gp.batchProcessed(new DataBatch(model.getData(), 0));
        assertEquals("Incorrect tick count after batch processed", 1, gp.getTotalGPUTime());
        assertEquals("Training queue should be size", 1, gp.getBatchTrainQueueLength());

        // We've inserted one batch, and then caused a tick, the batch should be removed now
        gp.tick();
        assertEquals("GPU tick count not updated", 1, gp.getTotalGPUTime());
        assertEquals("Batch not removed from training queue", 0, gp.getBatchTrainQueueLength());

        // Now process the second batch out of 2
        gp.batchProcessed(new DataBatch(model.getData(), 1000));
        gp.tick();
        assertEquals("GPU tick count not updated", 2, gp.getTotalGPUTime());
        assertEquals("Model should be trained", Model.Status.Trained, gp.getModel().getStatus());
        // Verify the callback was called
        assertTrue("Callback should be called", cb_called);
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
    public void batchProcessed() {
        GPU gp = new GPU("RTX2080");

        // The queue should not change if there is no model in training atm
        assertEquals("Batch training queue should be empty", 0, gp.getBatchTrainQueueLength());
        Model model = new Model("TestModel", "Tabular", 32 * 1000);
        gp.trainModel(model, null);
        DataBatch db = new DataBatch(model.getData(), 0);
        gp.batchProcessed(db);
        assertEquals("Batch training queue size", 1, gp.getBatchTrainQueueLength());
    }
}
