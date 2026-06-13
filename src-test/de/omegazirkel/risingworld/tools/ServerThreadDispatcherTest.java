package de.omegazirkel.risingworld.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ServerThreadDispatcherTest {

    @Test
    public void runsImmediatelyOnMainThread() {
        AtomicInteger calls = new AtomicInteger();
        List<Runnable> queued = new ArrayList<>();
        ServerThreadDispatcher dispatcher = new ServerThreadDispatcher(() -> true, queued::add, message -> {
        });

        assertTrue(dispatcher.dispatch(calls::incrementAndGet));
        assertEquals(1, calls.get());
        assertTrue(queued.isEmpty());
    }

    @Test
    public void enqueuesForeignThreadWork() {
        AtomicInteger calls = new AtomicInteger();
        List<Runnable> queued = new ArrayList<>();
        ServerThreadDispatcher dispatcher = new ServerThreadDispatcher(() -> false, queued::add, message -> {
        });

        assertTrue(dispatcher.dispatch(calls::incrementAndGet));
        assertEquals(0, calls.get());
        assertEquals(1, queued.size());

        queued.get(0).run();
        assertEquals(1, calls.get());
    }

    @Test
    public void rejectsNewAndQueuedWorkAfterClose() {
        AtomicInteger calls = new AtomicInteger();
        List<Runnable> queued = new ArrayList<>();
        ServerThreadDispatcher dispatcher = new ServerThreadDispatcher(() -> false, queued::add, message -> {
        });

        assertTrue(dispatcher.dispatch(calls::incrementAndGet));
        dispatcher.close();

        assertFalse(dispatcher.dispatch(calls::incrementAndGet));
        queued.get(0).run();
        assertEquals(0, calls.get());
    }

    @Test
    public void isolatesTaskAndEnqueueFailures() {
        List<String> errors = new ArrayList<>();
        ServerThreadDispatcher mainThreadDispatcher = new ServerThreadDispatcher(() -> true, task -> {
        }, errors::add);
        assertTrue(mainThreadDispatcher.dispatch(() -> {
            throw new IllegalStateException("task");
        }));

        ServerThreadDispatcher foreignThreadDispatcher = new ServerThreadDispatcher(() -> false, task -> {
            throw new IllegalStateException("enqueue");
        }, errors::add);
        assertFalse(foreignThreadDispatcher.dispatch(() -> {
        }));

        assertEquals(2, errors.size());
        assertTrue(errors.get(0).contains("task"));
        assertTrue(errors.get(1).contains("enqueue"));
    }
}
