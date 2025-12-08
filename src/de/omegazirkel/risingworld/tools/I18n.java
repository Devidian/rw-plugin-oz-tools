package de.omegazirkel.risingworld.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import net.risingworld.api.Plugin;

public class I18n {
    private static ConcurrentHashMap<String, I18n> instanceMap = new ConcurrentHashMap<String, I18n>();
    private Map<String, Properties> language = new HashMap<String, Properties>();
    private static final String defaultLanguage = "en";

    public static OZLogger logger() {
        return OZLogger.getInstance("OZ.Tools.i18n");
    }

    private static PluginSettings s = PluginSettings.getInstance();

    private I18n() {
    }

    public static I18n getInstanceMap() {
        logger().warn("getInstance called, response with default instance");
        return getInstance("default");
    }

    public static I18n getInstance(String pluginName) {
        if (!instanceMap.containsKey(pluginName)) {
            instanceMap.put(pluginName, new I18n());
        }
        return instanceMap.get(pluginName);
    }

    public static I18n getInstance(Plugin plugin) {
        logger().setLevel(s.logLevel);

        I18n instance = getInstance(plugin.getDescription("name"));
        instance.loadLanguageData(plugin.getPath());
        return instance;
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
                if (f.isFile() && f.getName().endsWith("properties")) {
                    String lang = f.getName().substring(0, 2);
                    // log.out("lang: "+lang);
                    Properties lngProperties = new Properties();
                    try {
                        in = new FileInputStream(f);
                        lngProperties.load(new InputStreamReader(in, "UTF8"));
                        in.close();
                        this.language.put(lang.toLowerCase(), lngProperties);
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
     * @param lang
     * @return
     */
    public String get(String key, String lang) {
        try {
            if (!this.language.containsKey(defaultLanguage)) {
                logger().error("no default language loaded. Failed to lookup " + key);
                return key;
            }
            Properties lngDefaultProperties = this.language.get(defaultLanguage);
            Properties lngProperties = null;
            if (!this.language.containsKey(lang.toLowerCase())) {
                lngProperties = lngDefaultProperties;
            } else {
                lngProperties = this.language.get(lang.toLowerCase());
            }
            return lngProperties.getProperty(key, lngDefaultProperties.getProperty(key, key));
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
}