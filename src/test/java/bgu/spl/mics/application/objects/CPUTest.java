package bgu.spl.mics.application.objects;

import static org.junit.Assert.*;
import org.junit.Test;

public class CPUTest {

    @Test
    public void tick() {
        CPU cp = new CPU(16, Cluster.getInstance());
        Data dt = new Data(Data.Type.Tabular, 1000);
        DataBatch db = new DataBatch(dt, 0);
        cp.processData(db);
        assertEquals("Base tick count incorrect", 2, cp.getTicksLeftForBatch());
        cp.tick();
        assertEquals("Incorrect tick count", 1, cp.getTicksLeftForBatch());
        assertNotNull("DataBatch processing ended prematurely", cp.getData());

        cp.tick();
        assertEquals("Incorrect tick count", 0, cp.getTicksLeftForBatch());
        assertNull("DataBatch processing not finished in time", cp.getData());
    }

    @Test
    public void getData() {
        CPU cp = new CPU(32, Cluster.getInstance());
        Data dt = new Data(Data.Type.Tabular, 1000);
        DataBatch db = new DataBatch(dt, 0);
        assertNull("Data not null before setting it", cp.getData());
        cp.processData(db);
        assertEquals("Data not successfully set", db, cp.getData());
    }

    @Test
    public void processData() {
        CPU cp_tab = new CPU(32, Cluster.getInstance());
        Data dt_tab = new Data(Data.Type.Tabular, 1000);
        DataBatch db_tab = new DataBatch(dt_tab, 0);
        cp_tab.processData(db_tab);
        assertEquals("DataBatch not set, tabular type", db_tab, cp_tab.getData());
        assertEquals("Incorrect tick count for tabular type", 1, cp_tab.getTicksLeftForBatch());

        CPU cp_txt = new CPU(32, Cluster.getInstance());
        Data dt_txt = new Data(Data.Type.Text, 1000);
        DataBatch db_txt = new DataBatch(dt_txt, 0);
        cp_txt.processData(db_txt);
        assertEquals("DataBatch not set, txt type", db_txt, cp_txt.getData());
        assertEquals("Incorrect tick count of txt data", 2, cp_txt.getTicksLeftForBatch());

        CPU cp_img = new CPU(32, Cluster.getInstance());
        Data dt_img = new Data(Data.Type.Images, 1000);
        DataBatch db_img = new DataBatch(dt_img, 0);
        cp_img.processData(db_img);
        assertEquals("DataBatch not set, img type", db_img, cp_img.getData());
        assertEquals("Incorrect tick count of img data", 4, cp_img.getTicksLeftForBatch());
    }
}