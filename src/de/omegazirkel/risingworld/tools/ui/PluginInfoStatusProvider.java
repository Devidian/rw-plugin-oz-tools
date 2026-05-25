package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.objects.Player;

public interface PluginInfoStatusProvider {
    String getPluginName();

    String getInfo(Player player);

    String getStatus(Player player);
}
