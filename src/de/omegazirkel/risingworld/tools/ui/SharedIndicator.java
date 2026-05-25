package de.omegazirkel.risingworld.tools.ui;

public final class SharedIndicator {
    private final String pluginName;
    private final String iconKey;

    SharedIndicator(String pluginName, String iconKey) {
        this.pluginName = pluginName;
        this.iconKey = iconKey;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getIconKey() {
        return iconKey;
    }
}
