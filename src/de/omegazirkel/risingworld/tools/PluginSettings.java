package de.omegazirkel.risingworld.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.logging.log4j.Level;

import de.omegazirkel.risingworld.OZTools;

public class PluginSettings {
	private static PluginSettings instance = null;

	private static OZTools plugin;

	private static OZLogger logger() {
		return OZLogger.getInstance("OZ.Tools.Settings");
	}

	// Settings
	public String logLevel = Level.DEBUG.name();
	public boolean reloadOnChange = false;
	public boolean enablePluginWelcomeMessage = false;
	public boolean enableSleepAnnouncement = false;

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
		Path settingsFile = Paths.get(filePath);
		Path defaultSettingsFile = settingsFile.resolveSibling("settings.default.properties");

		try {
			if (Files.notExists(settingsFile) && Files.exists(defaultSettingsFile)) {
				logger().info("settings.properties not found, copying from settings.default.properties...");
				Files.copy(defaultSettingsFile, settingsFile);
			}

			Properties settings = new Properties();
			if (Files.exists(settingsFile)) {
				try (FileInputStream in = new FileInputStream(settingsFile.toFile())) {
					settings.load(new InputStreamReader(in, "UTF8"));
				}
			} else {
				logger().warn(
						"⚠️ Neither settings.properties nor settings.default.properties found. Using default values.");
			}
			// fill global values
			logLevel = settings.getProperty("logLevel", "ALL");
			reloadOnChange = settings.getProperty("reloadOnChange", "false").contentEquals("true");

			// motd settings
			enablePluginWelcomeMessage = settings.getProperty("enablePluginWelcomeMessage", "false").contentEquals("true");
			enableSleepAnnouncement = settings.getProperty("enableSleepAnnouncement", "false").contentEquals("true");

			logger().info(plugin.getName() + " Plugin settings loaded");
			logger().info("Sending welcome message on login is: " + String.valueOf(enablePluginWelcomeMessage));
			logger().info("enablePluginWelcomeMessage is: " + enablePluginWelcomeMessage);
			logger().info("enableSleepAnnouncement is: " + enableSleepAnnouncement);
			logger().info("Loglevel is set to " + logLevel);
			logger().setLevel(logLevel);

		} catch (IOException ex) {
			logger().error("IOException on initSettings: " + ex.getMessage());
			ex.printStackTrace();
		} catch (NumberFormatException ex) {
			logger().error("NumberFormatException on initSettings: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
}
