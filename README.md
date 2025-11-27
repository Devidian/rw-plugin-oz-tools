# OZTools (Tools for other plugins)

This plugin has a set of utilities and libs used by different Plugins.

## Core Features

- Plugin Translation (i18n)
- Logger for console output
- Plugin-Change-Watcher for watching file changes in the Plugin folder
- Standard colors
- WebSocket Client
- SQLite DB Initializer (per Plugin)

## External libs

All librarys that were added to this plugin can be used for all plugins without shipping them included to the plugin.

- GSON
- jakarta.websocket-api
- jakarta.websocket-client-api
- tyrus-container-grizzly-client
- log4j-api
- log4j-core
- httpclient5
- httpclient5-fluent
- json-simple
- javacord

## Installation

Just extract the shared folder into your `Plugins` folder. The jar path should look like `Plugins/Tools/OZ-Tools.jar`

```css
    ── RisingWorld
        ├── Plugins
        │    ├── Tools
        │    │    ├── assets...
        │    │    ├── lib
        │    │    │    ├── gson-2.8.6.jar
        │    │    │    ├── javax.websocket-api-1.1.jar
        │    │    │    └── tyrus-standalone-client-1.15.jar
        │    │    ├── HISTORY.md
        │    │    ├── README.md
        │    │    ├── OZ-Tools.jar
        │    │    └── settings.properties

        :    :
```

## Feature details/examples

## Plugin translation (i18n)

This feature can be used to translate your plugin based on the current users language. Every player will see the plugin in his system language as long as the translation file exists.

### Integration

```java
import de.omegazirkel.risingworld.tools.I18n;

public class NewPlugin extends Plugin{

    // Init
    private static I18n t = null;
    @Override
    public void onEnable() {
        t = t != null ? t : new I18n(this);
    }

    // Usage example
    @EventMethod
    public void onPlayerSpawn(PlayerSpawnEvent event) {
        if (sendPluginWelcome) {
            Player player = event.getPlayer();
            String lang = player.getSystemLanguage();
            player.sendTextMessage(t.get("MSG_PLUGIN_WELCOME", lang));
        }
    }
}
```

### Translation files

```bash
_/Plugins/YourPluigin/i18n/en.properties
_/Plugins/YourPluigin/i18n/de.properties
_/Plugins/YourPluigin/i18n/__anyotherlanguage__.properties

```

### Example File content

```bash
MSG_PLUGIN_WELCOME=This Server uses <color=#F00000>NewPlugin</color> Plugin.\n\
Type <color=#997d4a>/np help</color> in chat for help.
```

## Logger

This feature uses Log4J framework for logging your plugin stuff in seperate log files.

```java
import de.omegazirkel.risingworld.tools.OZLogger;
import net.risingworld.api.Plugin;

public class NewPlugin extends Plugin {

    // Init
    private static OZLogger logger() {
        return OZLogger.getInstance("NewPlugin");
    }

    // Usage example
    @Override
    public void onEnable() {
        logger().info("✅ " + this.getName() + " Plugin is enabled version:" + this.getDescription("version"));
    }
}
```

## Colors

This is just a singleton class that holds some color values. The idea behind this is to have a default set of colors for the same stuff in different plugins (and not each plugin having its own colors for errors, warnings, infos, etc.)

```java
import de.omegazirkel.risingworld.tools.Colors;

public class NewPlugin extends Plugin {

    // Init
    static final Colors c = Colors.getInstance();

    // Usage example
    @EventMethod
    public void onPlayerCommand(PlayerCommandEvent event) {
        Player player = event.getPlayer();
        player.sendTextMessage(c.okay + pluginName + ":> " + c.endTag + "Your command was successfully ignored!");
    }
}
```

## Plugin-Change-Watcher

This static helper class creates 2 new threads to watch the filesystem for changes. To use it you have to implement `FileChangeListener` int your plugin and register your plugin to start watching changes.

### Example code

```java

import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.PluginChangeWatcher;

public class NewPlugin extends Plugin implements FileChangeListener{

    private static OZLogger logger() {
        return OZLogger.getInstance("NewPlugin");
    }
    static boolean flagRestart = false;

    @Override
    public void onEnable() {
        // your stuff
    }

    // Optional
    @Override
    public void onJarChanged(Path file) {
        logger().debug("Jar file changed: "+file.toString())
    }

    // Optional
    @Override
    public void onSettingsChanged(Path file) {
        logger().debug("settings.properties changed: "+file.toString())
        // this.initSettings();
    }

    // Optional
    @Override
    public void onOtherFileChanged(Path file) {
        logger().debug("File changed: "+file.toString())
        // react as you like
    }

}

```

## SQLite helper

... description coming soon ...

## WebSocket

... description coming soon ...
