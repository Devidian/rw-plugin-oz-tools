package de.omegazirkel.risingworld.tools;

import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class PluginReloadDebouncer {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "PluginReloadDebouncer-Thread"); t.setDaemon(true); return t; });
    private final AtomicReference<ScheduledFuture<?>> scheduledTask = new AtomicReference<>();
    private final Runnable reloadAction;
    private final long delay;
    private final TimeUnit unit;

    public PluginReloadDebouncer(Runnable reloadAction, long delay, TimeUnit unit) {
        this.reloadAction = reloadAction;
        this.delay = delay;
        this.unit = unit;
    }

    public void jarChanged(Path changedJar) {
        // If timer is active: cancel it
        ScheduledFuture<?> prev = scheduledTask.getAndSet(null);
        if (prev != null && !prev.isDone()) {
            prev.cancel(false);
        }

        // start new Timer
        ScheduledFuture<?> newTask = scheduler.schedule(() -> {
            try {
                reloadAction.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, delay, unit);

        scheduledTask.set(newTask);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
