package de.omegazirkel.risingworld.tools.ui;

import java.util.HashMap;

import net.risingworld.api.objects.Player;

public class CursorManager {
    private static HashMap<Player, Integer> refCounts = new HashMap<Player, Integer>();

    public static void show(Player p) {
        if (p == null)
            return;
        if (!refCounts.containsKey(p))
            refCounts.put(p, 0);
        int refCount = refCounts.get(p);
        if (refCount++ == 0)
            p.setMouseCursorVisible(true);
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
    }
}
