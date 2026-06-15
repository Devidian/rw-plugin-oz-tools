package de.omegazirkel.risingworld.tools;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class DiagnosticThreadFactory implements ThreadFactory {
    private final String owner;
    private final String purpose;
    private final String threadName;
    private final boolean daemon;
    private final Consumer<String> logger;
    private final AtomicInteger sequence = new AtomicInteger();

    public DiagnosticThreadFactory(String owner, String purpose, String threadName, boolean daemon,
            Consumer<String> logger) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.purpose = Objects.requireNonNull(purpose, "purpose");
        this.threadName = Objects.requireNonNull(threadName, "threadName");
        this.daemon = daemon;
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public Thread newThread(Runnable task) {
        Objects.requireNonNull(task, "task");
        int number = sequence.incrementAndGet();
        String name = number == 1 ? threadName : threadName + "-" + number;
        logger.accept(message("created", name));
        Thread thread = new Thread(() -> {
            logger.accept(message("started", name));
            try {
                task.run();
            } finally {
                logger.accept(message("ended", name));
            }
        }, name);
        thread.setDaemon(daemon);
        return thread;
    }

    private String message(String event, String name) {
        return "Thread " + event + ": owner=" + owner + ", purpose=" + purpose + ", name=" + name;
    }
}
