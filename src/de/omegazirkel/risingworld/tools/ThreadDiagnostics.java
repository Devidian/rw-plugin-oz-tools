package de.omegazirkel.risingworld.tools;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

public final class ThreadDiagnostics implements AutoCloseable {
    private static final int SUMMARY_SAMPLE_INTERVAL = 12;
    private final boolean enabled;
    private final SnapshotSource snapshotSource;
    private final Consumer<String> logger;
    private final LongSupplier clock;
    private ScheduledExecutorService scheduler;
    private final Map<Long, KnownThread> knownThreads = new HashMap<>();
    private int samples;

    public ThreadDiagnostics(boolean enabled, SnapshotSource snapshotSource, Consumer<String> logger,
            LongSupplier clock) {
        this.enabled = enabled;
        this.snapshotSource = Objects.requireNonNull(snapshotSource, "snapshotSource");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public static ThreadDiagnostics create(boolean enabled, Consumer<String> logger) {
        return new ThreadDiagnostics(enabled, ThreadDiagnostics::jvmSnapshot, logger, System::currentTimeMillis);
    }

    public synchronized void start() {
        if (!enabled || scheduler != null) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(new DiagnosticThreadFactory(
                "OZTools", "JVM thread diagnostics", "OZ-ThreadDiagnostics", true, logger));
        scheduler.scheduleAtFixedRate(this::sampleSafely, 0, 5, TimeUnit.SECONDS);
    }

    public synchronized boolean isRunning() {
        return scheduler != null && !scheduler.isShutdown();
    }

    void sample() {
        Snapshot snapshot = snapshotSource.snapshot();
        long now = clock.getAsLong();
        Map<Long, ThreadSnapshot> current = snapshot.threads().stream()
                .collect(Collectors.toMap(ThreadSnapshot::id, thread -> thread));

        for (ThreadSnapshot thread : snapshot.threads()) {
            if (!knownThreads.containsKey(thread.id())) {
                logger.accept(formatNewThread(thread));
                knownThreads.put(thread.id(), new KnownThread(thread.name(), now));
            }
        }
        List<Long> vanished = knownThreads.keySet().stream()
                .filter(id -> !current.containsKey(id))
                .toList();
        for (Long id : vanished) {
            KnownThread thread = knownThreads.remove(id);
            logger.accept("Thread disappeared: name=" + thread.name() + ", id=" + id
                    + ", lifetimeMs=" + Math.max(0, now - thread.firstSeenAtMs()));
        }
        samples++;
        if (samples % SUMMARY_SAMPLE_INTERVAL == 0) {
            logger.accept(formatSummary(snapshot));
        }
    }

    String formatSummary(Snapshot snapshot) {
        Map<String, Long> groups = snapshot.threads().stream()
                .collect(Collectors.groupingBy(ThreadSnapshot::name, Collectors.counting()));
        String grouped = groups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
        return "Thread summary: current=" + snapshot.currentCount() + ", peak=" + snapshot.peakCount()
                + ", totalStarted=" + snapshot.totalStartedCount() + ", names={" + grouped + "}";
    }

    private String formatNewThread(ThreadSnapshot thread) {
        String stack = thread.stackFrames().stream().limit(8).collect(Collectors.joining(" <- "));
        return "New thread: name=" + thread.name() + ", id=" + thread.id() + ", daemon=" + thread.daemon()
                + ", state=" + thread.state() + ", stack=[" + stack + "]";
    }

    private void sampleSafely() {
        try {
            sample();
        } catch (RuntimeException ex) {
            logger.accept("Thread diagnostics sample failed: " + ex.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
        knownThreads.clear();
    }

    private static Snapshot jvmSnapshot() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        Map<Long, Thread> liveThreads = Thread.getAllStackTraces().keySet().stream()
                .collect(Collectors.toMap(Thread::threadId, thread -> thread));
        List<ThreadSnapshot> threads = new ArrayList<>();
        for (ThreadInfo info : mxBean.getThreadInfo(mxBean.getAllThreadIds(), 8)) {
            if (info == null) {
                continue;
            }
            Thread thread = liveThreads.get(info.getThreadId());
            List<String> frames = List.of(info.getStackTrace()).stream()
                    .limit(8)
                    .map(StackTraceElement::toString)
                    .toList();
            threads.add(new ThreadSnapshot(info.getThreadId(), info.getThreadName(),
                    thread != null && thread.isDaemon(), info.getThreadState(), frames));
        }
        threads.sort(Comparator.comparingLong(ThreadSnapshot::id));
        return new Snapshot(List.copyOf(threads), mxBean.getThreadCount(), mxBean.getPeakThreadCount(),
                mxBean.getTotalStartedThreadCount());
    }

    @FunctionalInterface
    public interface SnapshotSource {
        Snapshot snapshot();
    }

    public record Snapshot(List<ThreadSnapshot> threads, int currentCount, int peakCount, long totalStartedCount) {
        public Snapshot {
            threads = List.copyOf(threads);
        }
    }

    public record ThreadSnapshot(long id, String name, boolean daemon, Thread.State state, List<String> stackFrames) {
        public ThreadSnapshot {
            stackFrames = List.copyOf(stackFrames);
        }
    }

    private record KnownThread(String name, long firstSeenAtMs) {
    }
}
