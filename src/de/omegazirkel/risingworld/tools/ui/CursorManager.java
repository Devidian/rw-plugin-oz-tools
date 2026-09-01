package de.omegazirkel.risingworld.tools.ui;

import java.util.HashMap;
import java.util.Map;

import net.risingworld.api.Timer;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;

public class CursorManager {
    private static HashMap<Player, Integer> refCounts = new HashMap<Player, Integer>();
    private static final Map<Player, UIElement> modalRoots = new HashMap<>();
    private static final Map<Player, Timer> modalCursorMonitors = new HashMap<>();

    public static void show(Player p) {
        if (p == null)
            return;
        if (!refCounts.containsKey(p))
            refCounts.put(p, 0);
        int refCount = refCounts.get(p);
        if (refCount++ == 0)
            p.setMouseCursorVisible(true);
        refCounts.put(p, refCount);
    }

    public static void hide(Player p) {
        if (p == null)
            return;
        if (!refCounts.containsKey(p))
            refCounts.put(p, 0);
        int refCount = refCounts.get(p);
        if (--refCount <= 0) {
            refCount = 0;
            p.setMouseCursorVisible(false);
        }
        refCounts.put(p, refCount);
    }

    /** Use when a top-level overlay is definitively closed. */
    public static void forceHide(Player player) {
        if (player == null) return;
        stopModalCursorMonitor(player);
        modalRoots.remove(player);
        refCounts.put(player, 0);
        player.setMouseCursorVisible(false);
        // Modal removal is applied asynchronously by the client; repeat after
        // the removal tick so its cursor state cannot overwrite this reset.
        new Timer(0.1f, 0.1f, 1, () -> player.setMouseCursorVisible(false)).start();
    }

    /** Tracks a root modal so Escape cleanup never hides the cursor for an open overlay. */
    public static void showModal(Player player, UIElement modalRoot) {
        if (player == null || modalRoot == null) return;
        stopModalCursorMonitor(player);
        modalRoots.put(player, modalRoot);
        show(player);
        Timer monitor = new Timer(0.1f, 0.1f, -1, () -> {
            if (!contains(player, modalRoot)) forceHide(player);
        });
        modalCursorMonitors.put(player, monitor);
        monitor.start();
    }

    /**
     * UITarget.Modal removes the element client-side on Escape without invoking
     * the overlay's close callback. Check after that removal has propagated.
     */
    public static void cleanupClosedModalAfterEscape(Player player) {
        if (player == null || !modalRoots.containsKey(player)) return;
        new Timer(0.05f, 0.05f, 1, () -> {
            UIElement modalRoot = modalRoots.get(player);
            if (modalRoot == null) return;
            forceHide(player);
        }).start();
    }

    public static void forgetModal(Player player, UIElement modalRoot) {
        if (player != null && modalRoots.get(player) == modalRoot) {
            modalRoots.remove(player);
            stopModalCursorMonitor(player);
        }
    }

    private static void stopModalCursorMonitor(Player player) {
        Timer monitor = modalCursorMonitors.remove(player);
        if (monitor != null && !monitor.isKilled()) monitor.kill();
    }

    private static boolean contains(Player player, UIElement element) {
        UIElement[] elements = player.getAllUIElements(true);
        if (elements == null) return false;
        for (UIElement current : elements) if (current == element) return true;
        return false;
    }
}
