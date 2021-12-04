package bgu.spl.mics;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.runners.Parameterized.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import java.util.Arrays;
import java.util.Collection;
import bgu.spl.mics.MicroService;


import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

@RunWith(Parameterized.class)
public class MessageBusTest {

    @Parameter(0)
    public MessageBus mb;

    public static Collection<Object[]> data() {
        MessageBus[] mb_array;
        mb_array[0] = MessageBusImpl.getInstance();
    }

    @Test
    public void testSubscribeEvent() {
        DummyMicroService m1 = new DummyMicroService("m_test");
        DummyEvent e1 = new DummyEvent();
        mb.subscribeEvent(e1.getClass(),m1);
        assertTrue("fail",mb.isSubscribedToMessage(e1.getClass(), m1));
    }

    @Test
    public void testSubscribeBroadcast() {
        DummyMicroService m = new DummyMicroService("m_test");
        DummyBroadCast b = new DummyBroadCast();
        mb.subscribeBroadcast(b.getClass(),m);
        assertTrue("fail",mb.isSubscribedToMessage(b.getClass(), m));
    }

    @Test
    public void testComplete() {
        DummyMicroService m = new DummyMicroService("m_test");
        DummyEvent e = new DummyEvent();
        String result = "result";
        assertNotNull("fail", mb.getEventFuture(e));
        assertFalse("fail" , mb.getEventFuture(e).isDone());
        mb.complete (e,result);
        assertTrue("fail" , mb.getEventFuture(e).isDone());
        assertEquals("fail",mb.getEventFuture(e).get(),result);
    }

    @Test
    public void testSendBroadcast() {
    }

    @Test
    public void testSendEvent() {
    }

    @Test
    public void testRegister() {
    }

    @Test
    public void testUnregister() {
    }

    @Test
    public void testAwaitMessage() {
    }
}
class DummyMicroService extends MicroService {

    public DummyMicroService (String name) {
        super(name);
    }
    protected void initialize() {
        System.out.println(getName() + " started");
        terminate();
    }
}

class DummyEvent implements Event<String> {

}

class DummyBroadCast implements Broadcast {

}

