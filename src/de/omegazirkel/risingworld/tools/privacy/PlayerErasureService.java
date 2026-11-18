package de.omegazirkel.risingworld.tools.privacy;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.PlayerSettings;
import de.omegazirkel.risingworld.tools.db.SQLiteConnectionFactory;
import net.risingworld.api.Server;
import net.risingworld.api.World;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.MessageBoxButtons;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntPredicate;

/** Runs confirmed erasure jobs on the game thread after the player has disconnected. */
public final class PlayerErasureService implements AutoCloseable {
    private static final List<String> REQUIRED_STEPS = List.of("admin-utils", "land-claim", "mail", "marketplace",
            "stargate", "factions", "service-npc", "gps", "shop", "rewards", "bosses", "wallet", "discord-local",
            "manager", "stargate-relay", "intercom");
    private record RegisteredStep(IntPredicate ready, PlayerErasureCoordinator.Step step) { }
    private static final Map<String, RegisteredStep> steps = new ConcurrentHashMap<>();
    private static volatile PlayerErasureService active;

    @FunctionalInterface
    public interface Registration extends AutoCloseable {
        @Override void close();
    }

    public static Registration register(String id, PlayerErasureCoordinator.Step step) {
        return register(id, ignored -> true, step);
    }

    public static Registration register(String id, IntPredicate ready, PlayerErasureCoordinator.Step step) {
        var registration = new RegisteredStep(Objects.requireNonNull(ready), Objects.requireNonNull(step));
        if (steps.putIfAbsent(id, registration) != null) throw new IllegalStateException("Duplicate erasure step " + id);
        return () -> steps.remove(id, registration);
    }

    public static boolean isPending(int playerDbId) {
        PlayerErasureService service = active;
        if (service == null) return false;
        try {
            return service.coordinator.isPending(playerDbId);
        } catch (SQLException ex) {
            OZTools.logger().warn("Could not read player erasure state: " + ex.getMessage());
            return true;
        }
    }

    private final OZTools plugin;
    private final I18n i18n;
    private final Connection connection;
    private final PlayerErasureCoordinator coordinator;
    private boolean running = true;

    public PlayerErasureService(OZTools plugin, I18n i18n, PlayerSettings playerSettings) throws SQLException {
        this.plugin = plugin;
        this.i18n = i18n;
        this.connection = SQLiteConnectionFactory.open(plugin, World.getName() + ".erasure");
        try {
            this.coordinator = new PlayerErasureCoordinator(connection, playerSettings);
        } catch (SQLException | RuntimeException ex) {
            connection.close();
            throw ex;
        }
        active = this;
        plugin.executeDelayed(15, this::processPending);
    }

    public void command(Player player, String[] arguments) {
        if (arguments.length == 3 && "--character".equals(arguments[2])) {
            message(player, "characterUnavailable");
        } else if (arguments.length != 2) {
            message(player, "usage");
        } else if (isPending(player.getDbID())) {
            disconnectPendingPlayer(player);
        } else if (!ready(player.getDbID())) {
            message(player, "unavailable");
        } else {
            player.showMessageBox(MessageBoxButtons.Yes_No, text("title", player), text("confirm", player), 0,
                    answer -> { if (Integer.valueOf(0).equals(answer)) confirm(player); });
        }
    }

    public void disconnectPendingPlayer(Player player) {
        player.kick(text("pending", player));
    }

    private void confirm(Player player) {
        if (!running || !player.isConnected()) return;
        if (!ready(player.getDbID())) {
            message(player, "unavailable");
            return;
        }
        try {
            coordinator.start(player.getDbID(), REQUIRED_STEPS);
        } catch (SQLException ex) {
            OZTools.logger().warn("Could not persist player erasure request: " + ex.getMessage());
            message(player, "unavailable");
            return;
        }
        disconnectPendingPlayer(player);
    }

    private static boolean ready(int playerDbId) {
        String testPlayer = System.getenv("OZTOOLS_ERASURE_TEST_DBID");
        if (!Integer.toString(playerDbId).equals(testPlayer)) return false;
        List<String> blockers = REQUIRED_STEPS.stream().filter(id -> {
            RegisteredStep registration = steps.get(id);
            if (registration == null) return true;
            try {
                return !registration.ready().test(playerDbId);
            } catch (RuntimeException ex) {
                OZTools.logger().warn("Player erasure prerequisite failed for " + id);
                return true;
            }
        }).toList();
        if (!blockers.isEmpty()) {
            OZTools.logger().info("Player erasure test prerequisites blocked by: " + String.join(", ", blockers));
        }
        return blockers.isEmpty();
    }

    private void processPending() {
        if (!running) return;
        try {
            Map<String, PlayerErasureCoordinator.Step> handlers = new HashMap<>();
            steps.forEach((id, registration) -> handlers.put(id, registration.step()));
            for (int id : coordinator.pendingPlayerIds()) {
                Player player = Server.getPlayerByDbID(id);
                if (player != null && player.isConnected()) continue;
                try {
                    coordinator.resume(id, handlers);
                } catch (Exception ex) {
                    OZTools.logger().warn("Player erasure step will be retried: " + ex.getClass().getSimpleName());
                }
            }
        } catch (SQLException ex) {
            OZTools.logger().warn("Could not list pending player erasure requests: " + ex.getMessage());
        } finally {
            if (running) plugin.executeDelayed(15, this::processPending);
        }
    }

    private String text(String key, Player player) {
        return i18n.get("tc.privacy.erasure." + key, player);
    }

    private void message(Player player, String key) {
        player.showInfoMessageBox(text("title", player), text(key, player));
    }

    @Override
    public void close() throws SQLException {
        running = false;
        if (active == this) active = null;
        connection.close();
    }
}
