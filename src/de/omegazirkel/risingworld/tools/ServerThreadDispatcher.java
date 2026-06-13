package de.omegazirkel.risingworld.tools;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicBoolean;

import net.risingworld.api.Plugin;

/**
 * Dispatches work from foreign callback threads onto the Rising World server
 * thread and rejects new work after shutdown.
 */
public final class ServerThreadDispatcher implements AutoCloseable {
    private final BooleanSupplier mainThreadCheck;
    private final Consumer<Runnable> enqueue;
    private final Consumer<String> errorLogger;
    private final AtomicBoolean active = new AtomicBoolean(true);

    public ServerThreadDispatcher(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.mainThreadCheck = plugin::isMainThread;
        this.enqueue = plugin::enqueue;
        this.errorLogger = message -> OZLogger.getInstance(plugin.getName()).fatal(message);
    }

    ServerThreadDispatcher(BooleanSupplier mainThreadCheck, Consumer<Runnable> enqueue, Consumer<String> errorLogger) {
        this.mainThreadCheck = Objects.requireNonNull(mainThreadCheck, "mainThreadCheck");
        this.enqueue = Objects.requireNonNull(enqueue, "enqueue");
        this.errorLogger = Objects.requireNonNull(errorLogger, "errorLogger");
    }

    public boolean dispatch(Runnable task) {
        Objects.requireNonNull(task, "task");
        if (!active.get()) {
            return false;
        }
        Runnable guardedTask = () -> {
            if (active.get()) {
                try {
                    task.run();
                } catch (RuntimeException ex) {
                    errorLogger.accept("Server-thread task failed: " + ex.getMessage());
                }
            }
        };
        if (mainThreadCheck.getAsBoolean()) {
            guardedTask.run();
        } else {
            try {
                enqueue.accept(guardedTask);
            } catch (RuntimeException ex) {
                errorLogger.accept("Server-thread dispatch failed: " + ex.getMessage());
                return false;
            }
        }
        return true;
    }

    public boolean isActive() {
        return active.get();
    }

    @Override
    public void close() {
        active.set(false);
    }
}
