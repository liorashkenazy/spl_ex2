package bgu.spl.mics.application.objects;

import static org.junit.Assert.*;
import org.junit.Test;

public class CPUTest {

    @Test
    public void tick() {
        CPU cp = new CPU(16);
        Data dt = new Data("Tabular", 1000);
        DataBatch db = new DataBatch(dt, 0);
        cp.addDataForProcessing(db);
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
        CPU cp = new CPU(32);
        Data dt = new Data("Tabular", 1000);
        DataBatch db = new DataBatch(dt, 0);
        assertNull("Data not null before setting it", cp.getData());
        cp.addDataForProcessing(db);
        assertEquals("Data not successfully set", db, cp.getData());
    }

    @Test
    public void processData() {
        CPU cp_tab = new CPU(32);
        Data dt_tab = new Data("Tabular", 2000);
        DataBatch db_tab = new DataBatch(dt_tab, 0);
        cp_tab.addDataForProcessing(db_tab);
        assertEquals("DataBatch not set, tabular type", db_tab, cp_tab.getData());
        assertEquals("Incorrect tick count for tabular type", 1, cp_tab.getTicksLeftForBatch());
        assertTrue("Tabular data is not in processing", cp_tab.isDataInProcessing(db_tab));
        DataBatch db_tab2 = new DataBatch(dt_tab, 1000);
        cp_tab.addDataForProcessing(db_tab2);
        assertTrue("Additional data batch is not in processing", cp_tab.isDataInProcessing(db_tab2));
        assertEquals("New processing affected old one", db_tab, cp_tab.getData());

        CPU cp_txt = new CPU(32);
        Data dt_txt = new Data("Text", 1000);
        DataBatch db_txt = new DataBatch(dt_txt, 0);
        cp_txt.addDataForProcessing(db_txt);
        assertEquals("DataBatch not set, txt type", db_txt, cp_txt.getData());
        assertEquals("Incorrect tick count of txt data", 2, cp_txt.getTicksLeftForBatch());
        assertTrue("TXT data is not in processing", cp_txt.isDataInProcessing(db_txt));

        CPU cp_img = new CPU(32);
        Data dt_img = new Data("Images", 1000);
        DataBatch db_img = new DataBatch(dt_img, 0);
        cp_img.addDataForProcessing(db_img);
        assertEquals("DataBatch not set, img type", db_img, cp_img.getData());
        assertEquals("Incorrect tick count of img data", 4, cp_img.getTicksLeftForBatch());
        assertTrue("Image data is not in processing", cp_img.isDataInProcessing(db_img));
    }

    @Test
    public void getTotalCPUTime() {
        CPU cp = new CPU(32);

        assertEquals("Incorrect initial CPU time", 0, cp.getTotalCPUTime());
        cp.tick();
        assertEquals("Incorrect CPU time after tick without data", 0, cp.getTotalCPUTime());

        Data dt = new Data("Tabular", 1000);
        DataBatch db = new DataBatch(dt, 0);
        cp.addDataForProcessing(db);
        cp.tick();
        assertEquals("Incorrect CPU time after tick with data", 1, cp.getTotalCPUTime());
    }
}