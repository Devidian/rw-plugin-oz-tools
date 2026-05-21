package de.omegazirkel.risingworld.tools.settings;

@FunctionalInterface
public interface AdminSettingsWriter {
    boolean write(String value);
}
