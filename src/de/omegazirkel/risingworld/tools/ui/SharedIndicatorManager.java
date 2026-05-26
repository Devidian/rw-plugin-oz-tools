package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.Server;
import net.risingworld.api.Timer;
import net.risingworld.api.objects.Player;

public final class SharedIndicatorManager {
    private static final float REFRESH_INTERVAL_SECONDS = 2.0f;
    private static Timer refreshTimer;

    private SharedIndicatorManager() {
    }

    public static synchronized void start() {
        stop();
        refreshTimer = new Timer(1.0f, REFRESH_INTERVAL_SECONDS, -1, SharedIndicatorManager::refreshAllPlayers);
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
