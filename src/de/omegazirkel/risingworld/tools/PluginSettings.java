package de.omegazirkel.risingworld.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.logging.log4j.Level;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsEntry;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsType;
import de.omegazirkel.risingworld.tools.settings.JsonSettingsFile;
import de.omegazirkel.risingworld.tools.settings.SettingsFileEditor;
import net.risingworld.api.World;

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
	public boolean threadDiagnosticsEnabled = false;
	public boolean automaticPluginUpdateCheck = false;
	public int pluginUpdateCheckDelaySeconds = 30;
	public int pluginUpdateCheckDelayBetweenPluginsSeconds = 3;
	/** Optional GitHub API token. Intentionally JSON-only: never expose secrets in the admin UI. */
	public String githubToken = "";
	public boolean allowExternalPluginRepositories = false;
	/** Allows opt-in attempts to replace installed plugin files on Windows. */
	public boolean allowWindowsUpdate = false;
	/** Native plugin HTTP routes are disabled unless an administrator explicitly opts in. */
	public boolean webAllowUnsecureRequests = false;
	public boolean webUseWebsockets = false;
	public String webWsTargetUrl = "wss://rw-servers.omega-zirkel.de/ws";
	private Path settingsFile;
	private java.util.Map<String, String> currentSettings = new LinkedHashMap<>();
	private java.util.Map<String, String> defaultSettings = new LinkedHashMap<>();

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
		Path pluginPath = Path.of(plugin.getPath() != null ? plugin.getPath() : ".");
		initSettings(pluginPath.resolve("settings." + safeWorldName() + ".json").toString());
	}

	public void initSettings(String filePath) {
		settingsFile = Path.of(filePath);
		Path defaultSettingsFile = settingsFile.resolveSibling("settings.default.json");
		Path legacySettingsFile = settingsFile.resolveSibling("settings.properties");

		try {
			if (JsonSettingsFile.migrateLegacyProperties(legacySettingsFile, settingsFile))
				logger().info("Migrated legacy settings.properties to " + settingsFile.getFileName());
			if (Files.notExists(settingsFile) && Files.exists(defaultSettingsFile))
				JsonSettingsFile.writeFlatAtomically(settingsFile, JsonSettingsFile.loadFlat(defaultSettingsFile));
			java.util.Map<String, String> settings = JsonSettingsFile.loadFlat(settingsFile);
			java.util.Map<String, String> defaults = JsonSettingsFile.loadFlat(defaultSettingsFile);
			if (settings.isEmpty() && defaults.isEmpty()) logger().warn("No JSON settings files found. Using defaults.");
			// fill global values
			logLevel = value(settings, defaults, "logLevel", "ALL");
			logInternal = value(settings, defaults, "logInternal", "false")
					.contentEquals("true");
			reloadOnChange = value(settings, defaults, "reloadOnChange", "true")
					.contentEquals("true");
			threadDiagnosticsEnabled = value(settings, defaults, "threadDiagnosticsEnabled", "false")
					.contentEquals("true");
			automaticPluginUpdateCheck = value(settings, defaults, "automaticPluginUpdateCheck", "false").contentEquals("true");
			allowExternalPluginRepositories = value(settings, defaults, "allowExternalPluginRepositories", "false").contentEquals("true");
			allowWindowsUpdate = value(settings, defaults, "allowWindowsUpdate", "false").contentEquals("true");
			webAllowUnsecureRequests = value(settings, defaults, "web.allowUnsecureRequests", "false").contentEquals("true");
			webUseWebsockets = value(settings, defaults, "web.useWebsockets", "false").contentEquals("true");
			webWsTargetUrl = value(settings, defaults, "web.wsTargetUrl", "wss://rw-servers.omega-zirkel.de/ws");
			pluginUpdateCheckDelaySeconds = Integer.parseInt(value(settings, defaults, "pluginUpdateCheckDelaySeconds", "30"));
			pluginUpdateCheckDelayBetweenPluginsSeconds = Math.max(0, Integer.parseInt(value(settings, defaults,
					"pluginUpdateCheckDelayBetweenPluginsSeconds", "3")));
			githubToken = value(settings, defaults, "githubToken", "").trim();

			// motd settings
			enablePluginWelcomeMessage = value(settings, defaults, "enablePluginWelcomeMessage", "false")
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
				AdminSettingsEntry.group("logging", "Logging"),
				entry("logLevel", "Log level", "Controls Tools logging verbosity.", AdminSettingsType.STRING),
				entry("logInternal", "Log internal", "If true, log output is printed to the default console.",
						AdminSettingsType.BOOLEAN),
				AdminSettingsEntry.group("runtime", "Runtime"),
				entry("reloadOnChange", "Reload on change", "If true, jar changes trigger delayed plugin reloads.",
						AdminSettingsType.BOOLEAN),
				entry("threadDiagnosticsEnabled", "Thread diagnostics",
						"If true, Tools samples and logs JVM-wide thread lifecycle information.",
						AdminSettingsType.BOOLEAN),
				AdminSettingsEntry.group("pluginUpdates", "Plugin updates",
						"Controls automatic checks and installation of public plugin updates."),
				entry("automaticPluginUpdateCheck", "Automatic update check", "Checks public GitHub releases after startup.", AdminSettingsType.BOOLEAN),
				entry("pluginUpdateCheckDelaySeconds", "Update-check delay", "Delay in seconds after startup.", AdminSettingsType.INTEGER),
				entry("pluginUpdateCheckDelayBetweenPluginsSeconds", "Update-check interval",
						"Delay in seconds between public GitHub requests.", AdminSettingsType.INTEGER),
				entry("allowExternalPluginRepositories", "Allow external repositories", "Allow public non-OZ GitHub repositories.", AdminSettingsType.BOOLEAN),
				entry("allowWindowsUpdate", "Allow Windows updates",
						"Attempt installed-plugin updates on Windows. Running plugins can still lock files and cause the update to fail.",
						AdminSettingsType.BOOLEAN),
				AdminSettingsEntry.group("playerMessages", "Player messages"),
				entry("enablePluginWelcomeMessage", "Welcome message",
						"If true, Tools sends a welcome message when a player joins.", AdminSettingsType.BOOLEAN));
	}

	private AdminSettingsEntry entry(String key, String label, String description, AdminSettingsType type) {
		return new AdminSettingsEntry(
				key,
				label,
				description,
				currentSettings.getOrDefault(key, defaultSettings.getOrDefault(key, "")),
				defaultSettings.getOrDefault(key, ""),
				type,
				false,
				value -> SettingsFileEditor.writeValue(settingsFile, key, value));
	}

	private static String value(java.util.Map<String, String> settings, java.util.Map<String, String> defaults,
			String key, String fallback) {
		return settings.getOrDefault(key, defaults.getOrDefault(key, fallback));
	}

	private static String safeWorldName() {
		String world = World.getName();
		return (world == null || world.isBlank() ? "default" : world).replaceAll("[^A-Za-z0-9._-]", "_");
	}
}
