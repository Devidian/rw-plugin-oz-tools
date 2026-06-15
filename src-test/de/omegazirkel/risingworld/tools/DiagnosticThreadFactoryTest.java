package de.omegazirkel.risingworld.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class DiagnosticThreadFactoryTest {
    @Test
    public void logsLifecycleAndConfiguresThread() throws Exception {
        List<String> messages = new ArrayList<>();
        AtomicBoolean ran = new AtomicBoolean();
        DiagnosticThreadFactory factory = new DiagnosticThreadFactory(
                "owner", "purpose", "Worker", false, messages::add);

        Thread thread = factory.newThread(() -> ran.set(true));
        assertEquals("Worker", thread.getName());
        assertFalse(thread.isDaemon());
        thread.start();
        thread.join();

        assertTrue(ran.get());
        assertEquals(3, messages.size());
        assertTrue(messages.get(0).contains("created"));
        assertTrue(messages.get(1).contains("started"));
        assertTrue(messages.get(2).contains("ended"));
    }

    @Test
    public void logsEndWithoutSwallowingTaskException() throws Exception {
        List<String> messages = new ArrayList<>();
        AtomicBoolean uncaught = new AtomicBoolean();
        Thread thread = new DiagnosticThreadFactory("owner", "purpose", "Worker", true, messages::add)
                .newThread(() -> {
                    throw new IllegalStateException("expected");
                });
        thread.setUncaughtExceptionHandler((ignored, error) -> uncaught.set(true));

        thread.start();
        thread.join();

        assertTrue(thread.isDaemon());
        assertTrue(uncaught.get());
        assertEquals(3, messages.size());
        assertTrue(messages.get(2).contains("ended"));
    }
}
