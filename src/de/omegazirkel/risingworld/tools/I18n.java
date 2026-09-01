package de.omegazirkel.risingworld.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.omegazirkel.risingworld.OZTools;
import net.risingworld.api.Plugin;
import net.risingworld.api.objects.Player;

public class I18n {
    private static ConcurrentHashMap<String, I18n> instanceMap = new ConcurrentHashMap<String, I18n>();
    private Map<String, Map<String, String>> language = new HashMap<String, Map<String, String>>();
    private String loadedPluginPath;
    private static final String defaultLanguage = "en";

    public static OZLogger logger() {
        return OZTools.logger();
    }

    private static PluginSettings s = PluginSettings.getInstance();

    private I18n() {
    }

    public static I18n getInstanceMap() {
        logger().warn("getInstance called, response with default instance");
        return getInstance("default");
    }

    public static I18n getInstance(String pluginName) {
        if (instanceMap == null)
            new I18n();
        if (!instanceMap.containsKey(pluginName)) {
            instanceMap.put(pluginName, new I18n());
        }
        return instanceMap.get(pluginName);
    }

    public static I18n getInstance(Plugin plugin) {
        logger().setLevel(s.logLevel);

        I18n instance = getInstance(plugin.getDescription("name"));
        instance.loadLanguageDataOnce(plugin.getPath());
        return instance;
    }

    private synchronized void loadLanguageDataOnce(String pluginPath) {
        if (pluginPath == null || pluginPath.isBlank() || pluginPath.equals(loadedPluginPath)) {
            return;
        }
        loadLanguageData(pluginPath);
        loadedPluginPath = pluginPath;
    }

    /**
     *
     * @param pluginPath
     */
    private void loadLanguageData(String pluginPath) {
        logger().debug("Loading language files from " + pluginPath + "/i18n'");
        File folder = new File(pluginPath + "/i18n");
        File[] listOfFiles = folder.listFiles();
        FileInputStream in;
        try {
            logger().debug("Files found: " + listOfFiles.length);
            for (File f : listOfFiles) {
                logger().debug("loading: " + f.getAbsolutePath());
                if (f.isFile() && f.getName().endsWith(".json")) {
                    String lang = f.getName().substring(0, 2);
                    try {
                        in = new FileInputStream(f);
                        JsonElement root = JsonParser.parseReader(new InputStreamReader(in, "UTF8"));
                        in.close();
                        if (!root.isJsonObject()) throw new IOException("Translation root must be an object");
                        Map<String, String> entries = new HashMap<String, String>();
                        flatten("", root.getAsJsonObject(), entries);
                        this.language.put(lang.toLowerCase(), entries);
                    } catch (FileNotFoundException e) {
                        logger().fatal("FileNotFoundException: " + e.getMessage());
                        e.printStackTrace();
                    } catch (IOException e) {
                        logger().fatal("IOException: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            logger().fatal("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * returns the language that is used for the given language
     *
     * @param lang
     * @return
     */
    public String getLanguageUsed(String lang) {
        if (!this.language.containsKey(lang.toLowerCase())) {
            return defaultLanguage + " (default Language)";
        } else {
            return lang;
        }
    }

    /**
     *
     * @return
     */
    public String getLanguageAvailable() {
        String[] keys = this.language.keySet().toArray(new String[0]);
        return String.join(", ", keys);
    }

    /**
     *
     * @param key
     * @param player
     * @return
     */
    public String get(String key, Player player) {
        return get(key, OZTools.getPlayerLanguage(player));
    }

    /**
     *
     * @param key
     * @param lang
     * @return
     */
    public String get(String key, String lang) {
        try {
            key = canonicalKey(key);
            if (!this.language.containsKey(defaultLanguage)) {
                logger().error("no default language loaded. Failed to lookup " + key);
                return key;
            }
            Map<String, String> lngDefaultProperties = this.language.get(defaultLanguage);
            Map<String, String> lngProperties = null;
            if (!this.language.containsKey(lang.toLowerCase())) {
                lngProperties = lngDefaultProperties;
            } else {
                lngProperties = this.language.get(lang.toLowerCase());
            }
            String leafKey = key + ".$value";
            return lngProperties.getOrDefault(key,
                    lngProperties.getOrDefault(leafKey, lngDefaultProperties.getOrDefault(key,
                            lngDefaultProperties.getOrDefault(leafKey, key))));
        } catch (Exception e) {
            logger().fatal("Exception: " + e.getMessage());
            e.printStackTrace();
            return key;
        }

    }

    /**
     *
     * @param key
     * @return
     */
    public String get(String key) {
        return this.get(key, defaultLanguage);
    }

    private static void flatten(String prefix, JsonObject object, Map<String, String> entries) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue().isJsonObject()) flatten(path, entry.getValue().getAsJsonObject(), entries);
            else if (entry.getValue().isJsonPrimitive()) entries.put(path, entry.getValue().getAsString());
            else throw new IllegalArgumentException("Unsupported translation value at " + path);
        }
    }

    private static String canonicalKey(String key) {
        return key != null && key.regionMatches(true, 0, "TC_", 0, 3)
                ? key.toLowerCase().replace('_', '.') : key;
    }
}
