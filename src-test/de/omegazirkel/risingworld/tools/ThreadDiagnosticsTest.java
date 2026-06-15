package de.omegazirkel.risingworld.tools;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class ThreadDiagnosticsTest {
    @Test
    public void detectsNewAndDisappearedThreads() {
        AtomicLong clock = new AtomicLong(1000);
        AtomicReference<ThreadDiagnostics.Snapshot> snapshot = new AtomicReference<>(snapshot(
                thread(7, "Worker", true)));
        List<String> messages = new ArrayList<>();
        ThreadDiagnostics diagnostics = new ThreadDiagnostics(true, snapshot::get, messages::add, clock::get);

        diagnostics.sample();
        clock.set(1042);
        snapshot.set(snapshot());
        diagnostics.sample();

        assertTrue(messages.get(0).contains("New thread: name=Worker"));
        assertTrue(messages.get(0).contains("stack=[frame-1 <- frame-2]"));
        assertTrue(messages.get(1).contains("Thread disappeared: name=Worker"));
        assertTrue(messages.get(1).contains("lifetimeMs=42"));
    }

    @Test
    public void summaryGroupsNamesAndReportsJvmCounts() {
        ThreadDiagnostics diagnostics = new ThreadDiagnostics(true, () -> snapshot(), message -> {
        }, System::currentTimeMillis);

        String summary = diagnostics.formatSummary(new ThreadDiagnostics.Snapshot(
                List.of(thread(1, "A", true), thread(2, "A", false), thread(3, "B", true)),
                3, 9, 20));

        assertTrue(summary.contains("current=3, peak=9, totalStarted=20"));
        assertTrue(summary.contains("A=2"));
        assertTrue(summary.contains("B=1"));
    }

    @Test
    public void disabledDiagnosticsDoNotStartAndStopIsIdempotent() {
        ThreadDiagnostics diagnostics = new ThreadDiagnostics(false, () -> snapshot(), message -> {
        }, System::currentTimeMillis);

        diagnostics.start();
        assertFalse(diagnostics.isRunning());
        diagnostics.close();
        diagnostics.close();
        assertFalse(diagnostics.isRunning());
    }

    private static ThreadDiagnostics.Snapshot snapshot(ThreadDiagnostics.ThreadSnapshot... threads) {
        return new ThreadDiagnostics.Snapshot(List.of(threads), threads.length, threads.length, threads.length);
    }

    private static ThreadDiagnostics.ThreadSnapshot thread(long id, String name, boolean daemon) {
        return new ThreadDiagnostics.ThreadSnapshot(id, name, daemon, Thread.State.RUNNABLE,
                List.of("frame-1", "frame-2"));
    }
}
