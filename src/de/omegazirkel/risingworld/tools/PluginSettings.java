package de.omegazirkel.risingworld.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Level;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsEntry;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsType;
import de.omegazirkel.risingworld.tools.settings.SettingsFileEditor;

public class PluginSettings {
	private static PluginSettings instance = null;

	private static OZTools plugin;

	private static OZLogger logger() {
		return OZTools.logger();
	}

	// Settings
	public String logLevel = Level.DEBUG.name();
	public boolean logInternal = false;
	public boolean reloadOnChange = false;
	public boolean enablePluginWelcomeMessage = false;
	private Path settingsFile;
	private Properties currentSettings = new Properties();
	private Properties defaultSettings = new Properties();

	// END Settings

	public static PluginSettings getInstance(OZTools p) {
		plugin = p;
		return getInstance();
	}

	public static PluginSettings getInstance() {

		if (instance == null) {
			instance = new PluginSettings();
		}
		return instance;
	}

	private PluginSettings() {
	}

	public void initSettings() {
		initSettings((plugin.getPath() != null ? plugin.getPath() : ".") + "/settings.properties");
	}

	public void initSettings(String filePath) {
		settingsFile = Paths.get(filePath);
		Path defaultSettingsFile = settingsFile.resolveSibling("settings.default.properties");

		try {
			if (Files.notExists(settingsFile) && Files.exists(defaultSettingsFile)) {
				logger().info("settings.properties not found, copying from settings.default.properties...");
				Files.copy(defaultSettingsFile, settingsFile);
			}

			Properties settings = new Properties();
			Properties defaults = new Properties();
			if (Files.exists(defaultSettingsFile)) {
				try (FileInputStream in = new FileInputStream(defaultSettingsFile.toFile())) {
					defaults.load(new InputStreamReader(in, "UTF8"));
				}
			}
			if (Files.exists(settingsFile)) {
				try (FileInputStream in = new FileInputStream(settingsFile.toFile())) {
					settings.load(new InputStreamReader(in, "UTF8"));
				}
			} else {
				logger().warn(
						"⚠️ Neither settings.properties nor settings.default.properties found. Using default values.");
			}
			// fill global values
			logLevel = settings.getProperty("logLevel", defaults.getProperty("logLevel", "ALL"));
			logInternal = settings.getProperty("logInternal", defaults.getProperty("logInternal", "false"))
					.contentEquals("true");
			reloadOnChange = settings.getProperty("reloadOnChange", defaults.getProperty("reloadOnChange", "true"))
					.contentEquals("true");

			// motd settings
			enablePluginWelcomeMessage = settings
					.getProperty("enablePluginWelcomeMessage",
							defaults.getProperty("enablePluginWelcomeMessage", "false"))
					.contentEquals("true");

			logger().info(plugin.getName() + " Plugin settings loaded");
			logger().info("Sending welcome message on login is: " + String.valueOf(enablePluginWelcomeMessage));
			logger().info("enablePluginWelcomeMessage is: " + enablePluginWelcomeMessage);
			logger().info("Loglevel is set to " + logLevel);
			logger().setLevel(logLevel);
			currentSettings = settings;
			defaultSettings = defaults;

		} catch (IOException ex) {
			logger().error("IOException on initSettings: " + ex.getMessage());
			ex.printStackTrace();
		} catch (NumberFormatException ex) {
			logger().error("NumberFormatException on initSettings: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public List<AdminSettingsEntry> adminSettingsEntries() {
		return Arrays.asList(
				entry("logLevel", "Log level", "Controls Tools logging verbosity.", AdminSettingsType.STRING),
				entry("logInternal", "Log internal", "If true, log output is printed to the default console.",
						AdminSettingsType.BOOLEAN),
				entry("reloadOnChange", "Reload on change", "If true, jar changes trigger delayed plugin reloads.",
						AdminSettingsType.BOOLEAN),
				entry("enablePluginWelcomeMessage", "Welcome message",
						"If true, Tools sends a welcome message when a player joins.", AdminSettingsType.BOOLEAN));
	}

	private AdminSettingsEntry entry(String key, String label, String description, AdminSettingsType type) {
		return new AdminSettingsEntry(
				key,
				label,
				description,
				currentSettings.getProperty(key, defaultSettings.getProperty(key, "")),
				defaultSettings.getProperty(key, ""),
				type,
				false,
				value -> SettingsFileEditor.writeValue(settingsFile, key, value));
	}
}
