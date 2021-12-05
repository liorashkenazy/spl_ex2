package bgu.spl.mics;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import bgu.spl.mics.MessageBusImpl;

import java.util.Arrays;
import java.util.Collection;
import bgu.spl.mics.MicroService;


import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class MessageBusTest {

    MessageBusImpl mb;

    @Before
    public void setUp() {
        mb = MessageBusImpl.getInstance();
    }

    @After
    public void tearDown() {
        LinkedList<MicroService> registered_ms = mb.getRegisteredServices();
        for (MicroService ms : registered_ms) {
            mb.unregister(ms);
        }
    }

    @Test
    public void testSubscribeEvent() {
        DummyMicroService m1 = new DummyMicroService("m1_test");
        DummyMicroService m2 = new DummyMicroService("m2_test");
        DummyMicroService m3 = new DummyMicroService("m3_test");
        DummyEventType1 e1 = new DummyEventType1();
        DummyEventType2 e2 = new DummyEventType2();
        mb.register(m1);
        mb.register(m2);
        mb.subscribeEvent(e1.getClass(),m1);
        mb.subscribeEvent(e2.getClass(),m1);
        mb.subscribeEvent(e1.getClass(),m2);
        mb.subscribeEvent(e1.getClass(),m3);
        assertTrue("Micro-Service not successfully subscribed event",mb.isSubscribedToMessage(e1.getClass(), m1));
        assertTrue("Micro-Service not successfully subscribed second event",mb.isSubscribedToMessage(e2.getClass(), m1));
        assertTrue("Second micro-Service not successfully subscribed event",mb.isSubscribedToMessage(e1.getClass(), m2));
        assertFalse("Micro-Service which isn't registered shouldn't successfully subscribed event",mb.isSubscribedToMessage(e1.getClass(), m3));
    }

    @Test
    public void testSubscribeBroadcast() {
        DummyMicroService m1 = new DummyMicroService("m1_test");
        DummyMicroService m2 = new DummyMicroService("m2_test");
        DummyMicroService m3 = new DummyMicroService("m3_test");
        DummyBroadCastType1 b1 = new DummyBroadCastType1();
        DummyBroadCastType2 b2 = new DummyBroadCastType2();
        mb.register(m1);
        mb.register(m2);
        mb.subscribeBroadcast(b1.getClass(),m1);
        mb.subscribeBroadcast(b2.getClass(),m1);
        mb.subscribeBroadcast(b1.getClass(),m2);
        mb.subscribeBroadcast(b1.getClass(),m3);
        assertTrue("Micro-Service not successfully subscribed broadcast",mb.isSubscribedToMessage(b1.getClass(), m1));
        assertTrue("Micro-Service not successfully subscribed second broadcast",mb.isSubscribedToMessage(b2.getClass(), m1));
        assertTrue("Second Micro-Service not successfully subscribed broadcast",mb.isSubscribedToMessage(b1.getClass(), m2));
        assertFalse("Micro-Service which isn't registered shouldn't successfully subscribed broadcast",mb.isSubscribedToMessage(b1.getClass(), m3));
    }

    @Test
    public void testComplete() {
        DummyMicroService m1 = new DummyMicroService("m1_test");
        DummyEventType1 e1 = new DummyEventType1();
        String result = "result";
        mb.register(m1);
        assertNotNull("There is no Future object for this event", mb.getEventFuture(e1));
        assertFalse("Future shouldn't be resolved before the completion his corresponding event" , mb.getEventFuture(e1).isDone());
        mb.complete (e1,result);
        assertTrue("Future isn't resolved after completion his corresponding event" , mb.getEventFuture(e1).isDone());
        assertEquals("Incorrect Future result",mb.getEventFuture(e1).get(),result);
    }

    @Test
    public void testSendBroadcast() {
        DummyMicroService m1 = new DummyMicroService("m1_test");
        DummyMicroService m2 = new DummyMicroService("m2_test");
        DummyMicroService m3 = new DummyMicroService("m2_test");
        DummyBroadCastType1 b1 = new DummyBroadCastType1();
        DummyBroadCastType2 b2 = new DummyBroadCastType2();
        DummyEventType1 e1 = new DummyEventType1();
        mb.register(m1);
        mb.register(m2);
        mb.register(m3);
        mb.subscribeBroadcast(b1.getClass(),m1);
        mb.subscribeBroadcast(b2.getClass(),m1);
        mb.subscribeBroadcast(b1.getClass(),m2);
        mb.subscribeEvent(e1.getClass(),m3);
        mb.sendBroadcast(b1);
        mb.sendEvent(e1);
        try{
            assertEquals("BroadCast not successfully sent to subscribed Micro-Services",b1,mb.awaitMessage(m1));
        } catch(InterruptedException e){}
        try{
            assertEquals("Second BroadCast not successfully sent to subscribed Micro-Services",b2,mb.awaitMessage(m1));
        } catch(InterruptedException e){}
        try{
            assertEquals("BroadCast not successfully sent to subscribed Micro-Services",b1,mb.awaitMessage(m2));
        } catch(InterruptedException e){}
        try{
            assertNotEquals("BroadCast shouldn't be sent to un-subscribed Micro-Services",b1,mb.awaitMessage(m3));
        } catch(InterruptedException e){}
    }

    @Test
    public void testSendEvent() {
        DummyMicroService m1 = new DummyMicroService("m1_test");
        DummyMicroService m2 = new DummyMicroService("m2_test");
        DummyEventType1 e1 = new DummyEventType1();
        DummyEventType2 e2 = new DummyEventType2();
        mb.register(m1);
        mb.register(m2);
        assertNull("0 subscribers to event - should return Null",mb.sendEvent(e1));
        mb.subscribeEvent(e1.getClass(),m1);
        mb.subscribeEvent(e1.getClass(),m2);
        mb.subscribeEvent(e2.getClass(),m2);
        MicroService m_first = mb.getNextServiceForEvent(e1.getClass());
        assertNotNull("Should return Future object",mb.sendEvent(e1));
        MicroService m_second = mb.getNextServiceForEvent(e1.getClass());
        assertNotEquals("Sending event should be in a round-robin manner",m_first,m_second);
        try{
            assertEquals("Event not successfully sent to Micro-service queue",e1,mb.awaitMessage(m1));
        } catch (InterruptedException e){}
        mb.sendEvent(e2);
        try{
            assertNotEquals("Event should be sent only to one Micro-service",e1,mb.awaitMessage(m2));
        } catch (InterruptedException e){}
    }

    @Test
    public void testRegister() {
        DummyMicroService m1 = new DummyMicroService("m1_test");
        mb.register(m1);
        assertTrue("Micro-Service not successfully registered MessageBus", mb.isRegistered(m1));
    }

    @Test
    public void testUnregister() {
        DummyMicroService m1 = new DummyMicroService("m1_test");
        DummyEventType1 e1 = new DummyEventType1();
        DummyBroadCastType1 b1 = new DummyBroadCastType1();
        mb.register(m1);
        mb.subscribeEvent(e1.getClass(),m1);
        mb.subscribeBroadcast(b1.getClass(),m1);
        mb.unregister(m1);
        assertFalse("Micro-Service not successfully unregistered MessageBus", mb.isRegistered(m1));
        assertFalse("Micro-Service is still subscribed to event type after unregistered", mb.isSubscribedToMessage(e1.getClass(),m1));
        assertFalse("Micro-Service is still subscribed to broadcast type after unregistered", mb.isSubscribedToMessage(b1.getClass(),m1));
    }

    @Test
    public void testAwaitMessage() {
        DummyMicroService m1 = new DummyMicroService("m1_test");
        DummyEventType1 e1 = new DummyEventType1();
        DummyEventType2 e2 = new DummyEventType2();
        boolean thrown_exception = false;
        try {
            mb.awaitMessage(m1);
        } catch (InterruptedException e){}
         catch (IllegalStateException e) {
            thrown_exception = true;
        }
        assertTrue("For unregistered Micro-Service should throw 'IllegalStateException'",thrown_exception);
        mb.register(m1);
        mb.subscribeEvent(e1.getClass(),m1);
        mb.subscribeEvent(e2.getClass(),m1);
        mb.sendEvent(e1);
        mb.sendEvent(e2);
        Message first_message = null;
        Message second_message = null;
        try {
            first_message = mb.awaitMessage(m1);

        }catch (InterruptedException e){} catch (IllegalStateException e){}
        try{
            second_message = mb.awaitMessage(m1);

        }catch (InterruptedException e){} catch (IllegalStateException e){}
        assertEquals("Incorrect event sent to Micro-service",first_message,e1);
        assertEquals("Incorrect event sent to Micro-service",second_message,e2);
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

class DummyEventType1 implements Event<String> {

}

class DummyEventType2 implements Event<String> {

}

class DummyBroadCastType1 implements Broadcast {

}

class DummyBroadCastType2 implements Broadcast {

}

