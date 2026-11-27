package de.omegazirkel.risingworld.tools.ui;

import java.util.List;

import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.PluginUpdateService;
import de.omegazirkel.risingworld.tools.ServerThreadDispatcher;
import net.risingworld.api.objects.Player;

/** Player-facing update notifications and callbacks for one active Tools runtime. */
public final class PluginUpdateActions {
    private final PluginUpdateService service;
    private final ServerThreadDispatcher dispatcher;
    private final I18n t;
    private final Runnable scheduleReload;

    public PluginUpdateActions(PluginUpdateService service, ServerThreadDispatcher dispatcher, I18n translations,
            Runnable scheduleReload) {
        this.service = service;
        this.dispatcher = dispatcher;
        this.t = translations;
        this.scheduleReload = scheduleReload;
    }

    public void checkPluginUpdates(Player player, Runnable onCompleted) {
        if (player == null || !player.isAdmin()) return;
        player.sendTextMessage(t.get("tc.plugin.update.check.started", player));
        service.checkAsync(pluginName -> dispatcher.dispatch(() -> player.sendTextMessage(
                t.get("tc.plugin.update.check.plugin", player).replace("PH_PLUGIN_NAME", pluginName))),
                ignored -> dispatcher.dispatch(() -> {
                    if (onCompleted != null) onCompleted.run();
                }),
                updatesAvailable -> dispatcher.dispatch(() -> {
                    player.sendTextMessage(t.get(service.isGitHubRateLimited() ? "tc.plugin.update.check.rate.limited"
                            : updatesAvailable ? "tc.plugin.update.check.updates.available" : "tc.plugin.update.check.none", player));
                    if (onCompleted != null) onCompleted.run();
                }));
    }

    public void checkPluginUpdate(String pluginName, Player player, Runnable onCompleted) {
        if (player == null || !player.isAdmin()) return;
        player.sendTextMessage(t.get("tc.plugin.update.check.plugin", player).replace("PH_PLUGIN_NAME", pluginName));
        service.checkPluginAsync(pluginName, ignored -> dispatcher.dispatch(() -> {
            player.sendTextMessage(service.isGitHubRateLimited()
                    ? t.get("tc.plugin.update.check.rate.limited", player)
                    : t.get("tc.plugin.update.check.plugin.completed", player).replace("PH_PLUGIN_NAME", pluginName));
            if (onCompleted != null) onCompleted.run();
        }));
    }

    private boolean rejectWindowsInstallation(Player player) {
        if (PluginUpdateService.isInstallationSupported(true)) return false;
        if (player != null) player.showWarningMessageBox(t.get("tc.plugin.update.windows.title", player),
                t.get("tc.plugin.update.windows.message", player));
        return true;
    }

    public void installPluginUpdate(String pluginName, Player player, Runnable onStateChanged) {
        // A null player represents a server-side call from a plugin.
        if (player != null && !player.isAdmin()) return;
        PluginUpdateService.Result result = service.results().get(pluginName);
        if ((result == null || result.state() != PluginUpdateService.State.NOT_INSTALLED)
                && rejectWindowsInstallation(player)) return;
        if (player != null && result != null) {
            player.sendTextMessage(t.get("tc.plugin.update.install.started", player)
                    .replace("PH_PLUGIN_NAME", pluginName)
                    .replace("PH_INSTALLED_VERSION", result.installedVersion())
                    .replace("PH_LATEST_VERSION", result.latestVersion()));
        }
        service.installLatestAsync(pluginName, () -> dispatcher.dispatch(() ->
                {
                    if (player != null) player.sendTextMessage(t.get("tc.plugin.update.install.completed", player));
                    service.markInstalledLatest(pluginName);
                    if (onStateChanged != null) onStateChanged.run();
                    scheduleReload.run();
                }), reason -> dispatcher.dispatch(() -> {
                    if (player != null) player.sendTextMessage(t.get("unsupported-windows".equals(reason) ? "tc.plugin.update.windows.message"
                            : "untrusted-release-source".equals(reason)
                            ? "tc.plugin.update.install.failed.untrusted.source" : "tc.plugin.update.install.failed", player));
                    if (onStateChanged != null) onStateChanged.run();
                }));
        if (onStateChanged != null) onStateChanged.run();
    }

    public void reinstallPlugin(String pluginName, Player player, Runnable onStateChanged) {
        PluginUpdateService.Result result = service.results().get(pluginName);
        if (player == null || !player.isAdmin() || result == null
                || result.state() != PluginUpdateService.State.CURRENT || rejectWindowsInstallation(player)) return;
        player.sendTextMessage(t.get("tc.plugin.update.reinstall.started", player).replace("PH_PLUGIN_NAME", pluginName)
                .replace("PH_LATEST_VERSION", result.latestVersion()));
        service.reinstallLatestAsync(pluginName, () -> dispatcher.dispatch(() -> {
            service.markInstalledLatest(pluginName);
            player.sendTextMessage(t.get("tc.plugin.update.reinstall.completed", player));
            if (onStateChanged != null) onStateChanged.run();
            scheduleReload.run();
        }), reason -> dispatcher.dispatch(() -> {
            player.sendTextMessage(t.get("tc.plugin.update.install.failed", player));
            if (onStateChanged != null) onStateChanged.run();
        }));
        if (onStateChanged != null) onStateChanged.run();
    }

    public void uninstallPlugin(String pluginName, Player player, Runnable onStateChanged) {
        if (player == null || !player.isAdmin() || rejectWindowsInstallation(player)) return;
        player.sendTextMessage(t.get("tc.plugin.update.uninstall.started", player).replace("PH_PLUGIN_NAME", pluginName));
        service.uninstallAsync(pluginName, () -> dispatcher.dispatch(() -> {
            player.sendTextMessage(t.get("tc.plugin.update.uninstall.completed", player));
            if (onStateChanged != null) onStateChanged.run();
            scheduleReload.run();
        }), reason -> dispatcher.dispatch(() -> {
            player.sendTextMessage(t.get("tc.plugin.update.uninstall.failed", player));
            if (onStateChanged != null) onStateChanged.run();
        }));
        if (onStateChanged != null) onStateChanged.run();
    }

    /** Installs confirmed releases one after another and reloads only after the
     * complete batch has been staged successfully. */
    public void installPluginUpdates(List<String> pluginNames, Player player, Runnable onStateChanged) {
        if (player == null || !player.isAdmin() || pluginNames == null) return;
        if (rejectWindowsInstallation(player)) return;
        List<String> pending = pluginNames.stream()
                .filter(name -> {
                    PluginUpdateService.Result result = service.results().get(name);
                    return result != null && result.state() == PluginUpdateService.State.UPDATE_AVAILABLE;
                }).toList();
        if (pending.isEmpty()) return;
        installPluginUpdatesNext(pending, 0, player, onStateChanged);
    }

    private void installPluginUpdatesNext(List<String> pending,
            int index, Player player, Runnable onStateChanged) {
        if (index >= pending.size()) {
            player.sendTextMessage(t.get("tc.plugin.update.install.completed", player));
            if (onStateChanged != null) onStateChanged.run();
            scheduleReload.run();
            return;
        }
        String pluginName = pending.get(index);
        PluginUpdateService.Result result = service.results().get(pluginName);
        if (result != null) {
            player.sendTextMessage(t.get("tc.plugin.update.install.started", player)
                    .replace("PH_PLUGIN_NAME", pluginName)
                    .replace("PH_INSTALLED_VERSION", result.installedVersion())
                    .replace("PH_LATEST_VERSION", result.latestVersion()));
        }
        service.installLatestAsync(pluginName, () -> dispatcher.dispatch(() -> {
            service.markInstalledLatest(pluginName);
            if (onStateChanged != null) onStateChanged.run();
            installPluginUpdatesNext(pending, index + 1, player, onStateChanged);
        }), reason -> dispatcher.dispatch(() -> {
            player.sendTextMessage(t.get("unsupported-windows".equals(reason) ? "tc.plugin.update.windows.message"
                            : "untrusted-release-source".equals(reason)
                    ? "tc.plugin.update.install.failed.untrusted.source" : "tc.plugin.update.install.failed", player));
            if (onStateChanged != null) onStateChanged.run();
        }));
    }
}
