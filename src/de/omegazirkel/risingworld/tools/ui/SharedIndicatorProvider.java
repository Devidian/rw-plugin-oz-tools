package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.objects.Player;

public interface SharedIndicatorProvider {
    boolean showIndicator(Player player);

    String getIcon(Player player);
}
