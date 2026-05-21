package de.omegazirkel.risingworld.tools.settings;

import java.util.List;

@FunctionalInterface
public interface AdminSettingsProvider {
    List<AdminSettingsEntry> entries();
}
