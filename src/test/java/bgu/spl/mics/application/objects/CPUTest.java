package bgu.spl.mics.application.objects;

import static org.junit.Assert.*;
import org.junit.Test;

public class CPUTest {

    private static boolean cb_called;

    private void setCBCalled(boolean b) {
        cb_called = b;
    }

    @Test
    public void tick() {
        CPU[] cp = {new CPU(16)};
        GPU[] gp = {new GPU("RTX3090")};
        Cluster cluster = Cluster.getInstance();
        cluster.setGPUs(gp);
        cluster.setCPUs(cp);
        gp[0].trainModel(new Model("model","Tabular",2000),(model1) -> setCBCalled(true));

        cp[0].tick();
        assertEquals("Base tick count incorrect",2, cp[0].getTicksLeftForBatch());
        assertNotNull("Cpu should get dataBatch to process",cp[0].getData());

        cp[0].tick();
        assertEquals("Incorrect tick count", 1, cp[0].getTicksLeftForBatch());
        assertEquals("Incorrect total cpu time count", 1, cp[0].getTotalCPUTime());
        assertNotNull("DataBatch processing ended prematurely", cp[0].getData());

        cp[0].tick();
        assertEquals("Incorrect total cpu time count", 2, cp[0].getTotalCPUTime());
        assertNotNull("After processing the first dataBatch, Cpu should get the second dataBatch to process",
                                                            cp[0].getData());
        assertEquals("Incorrect tick count for second dataBatch processing",
                                                    2,cp[0].getTicksLeftForBatch());

        cp[0].tick();
        cp[0].tick();
        assertEquals("Incorrect tick count after finish processing dataBatch", 0,
                                                                        cp[0].getTicksLeftForBatch());
        assertNull("Cpu has nothing to process, data should be null",cp[0].getData());

        int total_cpu_time = cp[0].getTotalCPUTime();
        cp[0].tick();
        assertEquals("Total cpu time shouldn't increase when cpu is idle", total_cpu_time,
                                                                                cp[0].getTotalCPUTime());
    }

    @Test
    public void getTickCountForDataType() {
        CPU cp1 = new CPU(32);
        Data dt1 = new Data("Tabular", 2000);
        DataBatch db1 = new DataBatch(dt1, 0, null);
        assertEquals("Incorrect tick count for tabular type", 1,
                cp1.getTickCountForDataType(db1.getData().getType()));

        Data dt2 = new Data("Text", 2000);
        DataBatch db2 = new DataBatch(dt2, 4, null);
        assertEquals("Incorrect tick count for text type", 2,
                cp1.getTickCountForDataType(db2.getData().getType()));

        Data dt3 = new Data("Images", 2000);
        DataBatch db3 = new DataBatch(dt3, 8, null);
        assertEquals("Incorrect tick count for images type", 4,
                                        cp1.getTickCountForDataType(db3.getData().getType()));
    }
}