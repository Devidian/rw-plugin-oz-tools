package de.omegazirkel.risingworld.tools.settings;

public class PlayerPluginAdminSettings {
    public final String pluginLabel;
    public final String pluginVersion;
    private final AdminSettingsProvider provider;
    private final AdminSettingsReloadAction reloadAction;

    public PlayerPluginAdminSettings(
            String pluginLabel,
            String pluginVersion,
            AdminSettingsProvider provider,
            AdminSettingsReloadAction reloadAction) {
        this.pluginLabel = pluginLabel;
        this.pluginVersion = pluginVersion;
        this.provider = provider;
        this.reloadAction = reloadAction;
    }

    public AdminSettingsProvider getProvider() {
        return provider;
    }

    public boolean canReload() {
        return reloadAction != null;
    }

    public void reload() {
        if (reloadAction != null) {
            reloadAction.reload();
        }
    }
}
