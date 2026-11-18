package de.omegazirkel.risingworld.tools;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.BiConsumer;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.privacy.PlayerErasureService;
import net.risingworld.api.objects.Player;

/** Server-local, purpose-specific player approval for external data transfers. */
public final class ExternalDataConsent {
    public enum Purpose {
        MANAGER("manager"), MAP("map"), DISCORD("discord"), STARGATE("stargate"), INTERCOM("intercom");

        private final String key;
        Purpose(String key) { this.key = key; }
    }

    public enum Status { PENDING, ACCEPTED, DECLINED }

    public record DeclinedPlayer(int dbId, Purpose purpose) { }

    public static final int TEXT_VERSION = 1;
    private static final String PREFIX = "oztools.externalConsent.";
    private static final ConcurrentHashMap<Purpose, BooleanSupplier> ACTIVE_PURPOSES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Purpose, CopyOnWriteArrayList<BiConsumer<Integer, Status>>> DECISION_LISTENERS = new ConcurrentHashMap<>();

    private ExternalDataConsent() { }

    /** Feature plugins declare whether their external player-data flow is active. */
    public static AutoCloseable registerPurpose(Purpose purpose, BooleanSupplier active) {
        if (purpose == null || active == null) throw new IllegalArgumentException("Purpose and provider required");
        ACTIVE_PURPOSES.put(purpose, active);
        return () -> ACTIVE_PURPOSES.remove(purpose, active);
    }

    /** Feature plugins can refresh external snapshots immediately after a player decision. */
    public static AutoCloseable registerDecisionListener(Purpose purpose, BiConsumer<Integer, Status> listener) {
        if (purpose == null || listener == null) throw new IllegalArgumentException("Purpose and listener required");
        CopyOnWriteArrayList<BiConsumer<Integer, Status>> listeners = DECISION_LISTENERS.computeIfAbsent(
                purpose, ignored -> new CopyOnWriteArrayList<>());
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public static List<Purpose> activePurposes() {
        return Arrays.stream(Purpose.values()).filter(purpose -> {
            BooleanSupplier active = ACTIVE_PURPOSES.get(purpose);
            try { return active != null && active.getAsBoolean(); }
            catch (RuntimeException ex) { return false; }
        }).toList();
    }

    public static List<DeclinedPlayer> declinedPlayers() {
        PlayerSettings settings = OZTools.playerSettings();
        if (settings == null) return List.of();
        return Arrays.stream(Purpose.values())
                .flatMap(purpose -> settings.playerIdsWithBoolean(key(purpose), false).stream()
                        .filter(dbId -> status(dbId, purpose) == Status.DECLINED)
                        .map(dbId -> new DeclinedPlayer(dbId, purpose)))
                .toList();
    }

    public static boolean isGranted(Player player, Purpose purpose) {
        return player != null && isGranted(player.getDbID(), purpose);
    }

    public static boolean isGranted(int dbId, Purpose purpose) {
        return status(dbId, purpose) == Status.ACCEPTED;
    }

    public static Status status(int dbId, Purpose purpose) {
        PlayerSettings settings = OZTools.playerSettings();
        if (settings == null || dbId <= 0 || purpose == null || PlayerErasureService.isPending(dbId)) return Status.PENDING;
        try {
            String key = key(purpose);
            if (settings.getInt(dbId, key + ".version").orElse(0) != TEXT_VERSION) return Status.PENDING;
            return settings.getBoolean(dbId, key).map(value -> value ? Status.ACCEPTED : Status.DECLINED)
                    .orElse(Status.PENDING);
        } catch (RuntimeException ex) {
            OZTools.logger().warn("External data consent lookup failed: " + ex.getMessage());
            return Status.PENDING;
        }
    }

    public static void decide(Player player, Purpose purpose, boolean accepted) {
        if (player == null) throw new IllegalArgumentException("Player required");
        decide(player.getDbID(), purpose, accepted);
    }

    public static void decide(int dbId, Purpose purpose, boolean accepted) {
        PlayerSettings settings = OZTools.playerSettings();
        if (settings == null || dbId <= 0 || purpose == null) throw new IllegalStateException("Consent store unavailable");
        if (PlayerErasureService.isPending(dbId)) return;
        String key = key(purpose);
        // Fail closed: an interrupted write cannot leave an accepted current version.
        settings.setInt(dbId, key + ".version", 0);
        settings.setBoolean(dbId, key, accepted);
        settings.setString(dbId, key + ".decidedAt", Instant.now().toString());
        settings.setInt(dbId, key + ".version", TEXT_VERSION);
        Status status = accepted ? Status.ACCEPTED : Status.DECLINED;
        for (BiConsumer<Integer, Status> listener : DECISION_LISTENERS.getOrDefault(purpose,
                new CopyOnWriteArrayList<>())) {
            try { listener.accept(dbId, status); }
            catch (RuntimeException ex) {
                OZTools.logger().warn("External data consent listener failed: " + ex.getMessage());
            }
        }
    }

    private static String key(Purpose purpose) { return PREFIX + purpose.key; }
}
