package de.omegazirkel.risingworld.tools.ui;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import de.omegazirkel.risingworld.OZTools;
import net.risingworld.api.Server;
import net.risingworld.api.Timer;
import net.risingworld.api.objects.Player;

public final class SharedIndicatorManager {
    private static final float REFRESH_INTERVAL_SECONDS = 2.0f;
    private static Timer refreshTimer;
    private static final AtomicBoolean timerContextVerified = new AtomicBoolean(false);

    private SharedIndicatorManager() {
    }

    public static synchronized void start(BooleanSupplier mainThreadCheck, Consumer<Runnable> serverThreadDispatcher) {
        stop();
        timerContextVerified.set(false);
        refreshTimer = new Timer(1.0f, REFRESH_INTERVAL_SECONDS, -1, () -> {
            boolean mainThread = mainThreadCheck.getAsBoolean();
            if (timerContextVerified.compareAndSet(false, true)) {
                if (mainThread) {
                    OZTools.logger().info("PluginAPI Timer callback verified on the server thread.");
                } else {
                    OZTools.logger().warn("PluginAPI Timer callback is not on the server thread; dispatching UI refresh.");
                }
            }
            serverThreadDispatcher.accept(SharedIndicatorManager::refreshAllPlayers);
        });
        refreshTimer.start();
        refreshAllPlayers();
    }

    public static synchronized void stop() {
        if (refreshTimer != null) {
            refreshTimer.kill();
            refreshTimer = null;
        }
        removeAllPlayers();
    }

    public static void refreshAllPlayers() {
        Player[] players = Server.getAllPlayers();
        if (players == null) {
            return;
        }
        for (Player player : players) {
            refresh(player);
        }
    }

    public static void refresh(Player player) {
        SharedIndicatorPanel.refresh(player);
    }

    public static void hide(Player player) {
        SharedIndicatorPanel.remove(player);
    }

    private static void removeAllPlayers() {
        Player[] players = Server.getAllPlayers();
        if (players == null) {
            return;
        }
        for (Player player : players) {
            SharedIndicatorPanel.remove(player);
        }
    }
}
