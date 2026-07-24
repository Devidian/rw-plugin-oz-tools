package de.omegazirkel.risingworld;

import java.nio.file.Path;
import java.util.List;

import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.PlayerSettings;
import de.omegazirkel.risingworld.tools.PluginSettings;
import de.omegazirkel.risingworld.tools.PluginUpdateService;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.events.player.PlayerDisconnectEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.events.player.ui.PlayerToggleInventoryEvent;
import net.risingworld.api.events.player.ui.PlayerUITextFieldChangeEvent;
import net.risingworld.api.objects.Player;

/** Rising World entry point; shared runtime behavior lives in {@link OZToolsRuntime}. */
public final class OZTools extends OZToolsRuntime implements Listener, FileChangeListener {
    public static void checkPluginUpdates() { OZToolsRuntime.checkPluginUpdates(); }
    public static void checkPluginUpdates(Player player) { OZToolsRuntime.checkPluginUpdates(player); }
    public static void checkPluginUpdates(Player player, Runnable onCompleted) {
        OZToolsRuntime.checkPluginUpdates(player, onCompleted);
    }
    public static void checkPluginUpdate(String pluginName, Player player, Runnable onCompleted) {
        OZToolsRuntime.checkPluginUpdate(pluginName, player, onCompleted);
    }
    public static PluginUpdateService.Result pluginUpdateResult(String pluginName) {
        return OZToolsRuntime.pluginUpdateResult(pluginName);
    }
    public static String getPlayerLanguage(Player player) { return OZToolsRuntime.getPlayerLanguage(player); }
    public static void installPluginUpdate(String pluginName, Player player, Runnable onStateChanged) {
        OZToolsRuntime.installPluginUpdate(pluginName, player, onStateChanged);
    }
    public static void installPluginUpdates(List<String> pluginNames, Player player, Runnable onStateChanged) {
        OZToolsRuntime.installPluginUpdates(pluginNames, player, onStateChanged);
    }
    public static OZLogger logger() { return OZToolsRuntime.logger(); }
    public static PluginSettings getSettings() { return OZToolsRuntime.getSettings(); }
    public static PlayerSettings playerSettings() { return OZToolsRuntime.playerSettings(); }

    @Override
    public void onEnable() {
        super.onEnable();
        registerEventListener(this);
    }

    @Override public void onDisable() { super.onDisable(); }
    @Override public void onSettingsChanged(Path settingsPath) { super.onSettingsChanged(settingsPath); }

    @Override @EventMethod
    public void onPlayerSpawn(PlayerSpawnEvent event) { super.onPlayerSpawn(event); }
    @Override @EventMethod
    public void onPlayerConnect(PlayerConnectEvent event) { super.onPlayerConnect(event); }
    @Override @EventMethod
    public void onPlayerDisconnect(PlayerDisconnectEvent event) { super.onPlayerDisconnect(event); }
    @Override @EventMethod
    public void onPlayerToggleInventory(PlayerToggleInventoryEvent event) { super.onPlayerToggleInventory(event); }
    @Override @EventMethod
    public void onPlayerUITextFieldChange(PlayerUITextFieldChangeEvent event) {
        super.onPlayerUITextFieldChange(event);
    }
    @Override @EventMethod
    public void onPlayerCommand(PlayerCommandEvent event) { super.onPlayerCommand(event); }
}
