package bgu.spl.mics;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class FutureTest {

    Future<String> future;
    boolean checks_get_blocks = false;
    String result;
    String returned_value;


    @Before
    public void setUp() {
        future = new Future<String>();
        result = "result";
        returned_value = "";
    }

    @Test
    public void testGet() {
        Thread t1 = new Thread(() -> {
            returned_value = future.get();
            checks_get_blocks = true;
        } );
        t1.start();
        assertFalse("Method should be blocking",checks_get_blocks);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e ) {}
        assertFalse("Method should be blocking",checks_get_blocks);
        future.resolve(result);
        try {
            t1.join();
        } catch (Exception e) {}
        assertEquals("Incorrect result value",result,returned_value);
    }

    @Test
    public void testResolve() {
        future.resolve(result);
        assertEquals("Incorrect result",result, future.get(0, TimeUnit.MILLISECONDS));
        assertTrue("After resolve the method 'isDone' should return true", future.isDone());
    }

    @Test
    public void testIsDone() {
        assertFalse("The object hasn't been resolved, should return false",future.isDone());
        future.resolve(result);
        assertTrue("The object has been resolved, should return true",future.isDone());
    }

    @Test
    public void testGetWithArgs() {
        long time_before = System.currentTimeMillis();
        assertNull("Result not available after waiting, should return null",future.get(100,TimeUnit.MILLISECONDS));
        long time_after = System.currentTimeMillis();
        assertTrue("Should wait at least 100 milliseconds",time_after-time_before > 100);
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e ) {}
            future.resolve(result);
        } );
        t1.start();
        String returned_value = future.get(2500,TimeUnit.MILLISECONDS);
        assertNotNull("Result available after waiting, shouldn't return null",returned_value);
        assertEquals("Incorrect result value",result,returned_value);
    }
}
