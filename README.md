# OZTools (Tools for other plugins)

This plugin has a set of utilities and libs used by different Plugins.

## Core Features

- Plugin Translation (i18n)
- Logger for console output
- Plugin-Change-Watcher for watching file changes in the Plugin folder
- Standard colors
- WebSocket Client
- SQLite DB Initializer (per Plugin)
- Player database lookup helper for recently seen players and best-effort player records

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

Just extract the plugin into your `Plugins` folder. The jar path should look like `Plugins/OZTools/OZTools.jar`

```css
    ── RisingWorld
        ├── Plugins
        │    ├── OZTools
        │    │    ├── i18n
        │    │    │    ├── de.properties
        │    │    │    ├── en.properties
        │    │    │    :
        │    │    ├── lib
        │    │    │    ├── *.jar
        │    │    │    :
        │    │    ├── HISTORY.md
        │    │    ├── OZTools.jar
        │    │    ├── README.md
        │    │    ├── settings.default.properties
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
        if (s.sendPluginWelcome) {
            Player player = event.getPlayer();
            String lang = player.getSystemLanguage();
            player.sendTextMessage(t.get("TC_MSG_PLUGIN_WELCOME", lang)
                    .replace("PH_PLUGIN_NAME", getDescription("name"))
                    .replace("PH_PLUGIN_VERSION", getDescription("version")));
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
TC_MSG_PLUGIN_WELCOME=ℹ️ <color=#F00000>PH_PLUGIN_NAME</color> | <color=#F0F000>vPH_PLUGIN_VERSION</color> | <color=#C0C0C0>/PH_PLUGIN_CMD</color> | loaded!

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

Use `SQLiteConnectionFactory.open(plugin)` for world-scoped plugin SQLite access.
The old `tools.db.SQLite` wrapper has been removed; plugin code should keep ownership
of its domain schema and pass the shared `Connection` into `PlayerSettings` or
plugin-local stores as needed.

## WebSocket

`WSClientEndpoint` invokes `WebSocketHandler` callbacks on WebSocket-owned
threads. Handlers may parse messages and perform transport work there, but must
dispatch through `ServerThreadDispatcher` before accessing Rising World API
objects or static game APIs. Do not retain `Player`, `Area`, world, inventory,
or UI objects in asynchronous callbacks.

`ServerThreadDispatcher` executes immediately on the server thread or delegates
to PluginAPI `enqueue(...)` from foreign threads. It owns no additional queue,
rejects new and already-enqueued work after `close()`, and isolates task and
enqueue exceptions. Callback producers should pass immutable values or stable
identifiers instead of Rising World API objects.

## Radial Main menu

Plugins can register entries in the shared `/ozt` radial menu with
`PluginMenuManager.registerPluginMenu(...)`. Plugin-owned child radial menus
should use Tools-registered shared icons where appropriate. The shared
Info/Status radial action uses the `icon-ki-info-status` icon key; plugins should
not register duplicate copies of that icon.

Plan 04 shortcut visibility is player-aware and defaults to visible. Plugins can
register a predicate with `PluginShortcutVisibility.register(pluginName,
predicate)` and should use `PluginShortcutVisibility.playerSettingKey(pluginName)`
as the persisted player-setting key when they expose a hide/show shortcut
setting. The shared `/ozt` menu and inventory panel use the same visibility
decision.

Tools registers these shared icon keys by default:

- `icon-ki-info-status`: shared Info/Status radial-menu action icon
- `icon-ki-placeholder`: generic placeholder for future features without a dedicated icon
- `icon-ki-soon`: generic placeholder for future unavailable or planned features

## Player-Plugin-Settings

... description coming soon ...

## Inventory overlay buttons

Plugins can register compact inventory entrypoints with
`InventoryOverlayButtons.registerButton(pluginName, label, iconKey, callback)`.
The shared inventory panel renders larger icons with compact labels, sorted by
plugin name and label. The current Rising World UI API does not expose a native
hover-tooltip property for custom UI elements, so labels are rendered inline
instead of hidden hover-only text.

Players can hide the compact labels from `/ozt config` in the OZTools player
settings. Icons remain visible and keep the same ordering.

Tools registers its own inventory button and opens the shared Tools Info/Status
panel from it. The panel refreshes already-open inventories when buttons are
registered or removed.

## Shared indicators

Plugins can register lightweight HUD indicators with `SharedIndicators`. Tools
hides the shared indicator panel while the player's inventory overlay is open
and refreshes indicators again when the inventory closes.

## CursorManager

... description coming soon ...

## PluginMenuManager

You can hool your plugin into the player-plugin-settings overlay

### Example code

```java

public class YourPlugin extends Plugin{

    @Override
	public void onEnable() {
        // ... your stuff ...
		// register plugin settings
		PlayerPluginSettingsOverlay.registerPlayerPluginSettings(
                new YourPluginPlayerPluginSettings(getDescription("version")));
        // ... your stuff ...
	}
}
```

```java
import de.omegazirkel.risingworld.tools.ui.PlayerPluginSettings;

public class YourPluginPlayerPluginSettings extends PlayerPluginSettings {

    public YourPluginPlayerPluginSettings(String pluginVersion) {
        this.pluginLabel = YourPlugin.name;
        this.pluginVersion = pluginVersion;
    }

    @Override
    public BasePlayerPluginSettingsPanel createPlayerPluginSettingsUIElement(Player uiPlayer) {
        return new YourPluginPlayerPluginSettingsPanel(uiPlayer, pluginLabel);
    }

}
```

```java

import de.omegazirkel.risingworld.tools.ui.BasePlayerPluginSettingsPanel;

public class YourPluginPlayerPluginSettingsPanel extends BasePlayerPluginSettingsPanel {

    public YourPluginPlayerPluginSettingsPanel(Player uiPlayer, String pluginLabel) {
        super(uiPlayer, pluginLabel);
    }

    @Override
    protected void redrawContent() {
        flexWrapper.removeAllChilds();
        // TODO: implement actual settings content for YourPlugin
    }

}

```

Alternative implementation (shorter)

```java

import de.omegazirkel.risingworld.tools.ui.BasePlayerPluginSettingsPanel;
import de.omegazirkel.risingworld.tools.ui.PlayerPluginSettings;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UIScrollView.ScrollViewMode;

public class YourPluginPlayerPluginSettings extends PlayerPluginSettings {

    public YourPluginPlayerPluginSettings() {
        this.pluginLabel = YourPlugin.name;
    }

    @Override
    public BasePlayerPluginSettingsPanel createPlayerPluginSettingsUIElement(Player uiPlayer) {
        return new BasePlayerPluginSettingsPanel(uiPlayer, pluginLabel) {
            @Override
            protected void redrawContent() {
                flexWrapper.removeAllChilds();
                // TODO: implement actual settings content for YourPlugin
            }

        };
    }

}


```

### Admin PluginSettings tab

Plugins can register admin-only `settings.properties` metadata for the shared
`PluginSettings` tab. Tools renders the tab only for admins and only for plugins
that register metadata. Sensitive settings must be omitted or marked sensitive;
editable values are limited to booleans, integers, and strings.

```java
import de.omegazirkel.risingworld.tools.settings.AdminSettingsEntry;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsType;
import de.omegazirkel.risingworld.tools.settings.PlayerPluginAdminSettings;
import de.omegazirkel.risingworld.tools.settings.SettingsFileEditor;
import de.omegazirkel.risingworld.tools.ui.PlayerPluginSettingsOverlay;

// inside onEnable(), after settings have been initialized
PlayerPluginSettingsOverlay.registerPlayerPluginAdminSettings(
        new PlayerPluginAdminSettings(
                YourPlugin.name,
                getDescription("version"),
                () -> List.of(new AdminSettingsEntry(
                        "enableFeature",
                        "Enable feature",
                        "Turns the feature on or off.",
                        String.valueOf(settings.enableFeature),
                        "false",
                        AdminSettingsType.BOOLEAN,
                        false,
                        value -> SettingsFileEditor.writeValue(settingsPath, "enableFeature", value))),
                () -> settings.initSettings()));
```

Use `AdminSettingsEntry.group(...)` to add labeled separators between related
settings. Group labels and descriptions use the same generated i18n lookup as
regular entries: `TC_SETTING_<KEY>_LABEL` and `TC_SETTING_<KEY>_DESC`.

```java
() -> List.of(
        AdminSettingsEntry.group("general", "General Settings"),
        new AdminSettingsEntry(
                "enableFeature",
                "Enable feature",
                "Turns the feature on or off.",
                String.valueOf(settings.enableFeature),
                "false",
                AdminSettingsType.BOOLEAN,
                false,
                value -> SettingsFileEditor.writeValue(settingsPath, "enableFeature", value)));
```

### Shared UI integration rules

Feature plugins should register shared UI hooks during `onEnable()` after their
settings and assets have been initialized. Unregister plugin-owned hooks during
`onDisable()` when the API provides an unregister method. Keep registration
content plugin-specific; reusable UI infrastructure belongs in Tools.

#### Inventory overlay buttons

Use `InventoryOverlayButtons` for compact actions shown below the player
inventory. Tools sorts registered buttons by plugin name and label, so plugins
must use stable labels and stable plugin names.

```java
import de.omegazirkel.risingworld.tools.ui.InventoryOverlayButtons;

InventoryOverlayButtons.registerButton(
        YourPlugin.name,
        "Open",
        "your-plugin-icon",
        event -> openPluginUi(event.getPlayer()));

// onDisable()
InventoryOverlayButtons.unregisterButtons(YourPlugin.name);
```

Rules:

- `pluginName` and `label` must not be blank.
- `iconKey` is optional; when supplied, the icon must be loaded through
  `AssetManager`.
- Button callbacks must stay lightweight and should open plugin-owned UI or
  delegate to plugin-owned services.

#### Shared indicators

Use `SharedIndicators` for small HUD indicators that are visible only when a
provider says they should be visible. Tools refreshes indicators on player
connect/spawn and when providers are registered or unregistered.

```java
import de.omegazirkel.risingworld.tools.ui.SharedIndicatorProvider;
import de.omegazirkel.risingworld.tools.ui.SharedIndicators;

SharedIndicators.registerProvider(YourPlugin.name, new SharedIndicatorProvider() {
    @Override
    public boolean showIndicator(Player player) {
        return isPlayerInPluginArea(player);
    }

    @Override
    public String getIcon(Player player) {
        return "your-plugin-icon";
    }
});

// onDisable()
SharedIndicators.unregisterProvider(YourPlugin.name);
```

Rules:

- Providers must not throw; Tools catches provider failures and hides the
  failing indicator.
- `getIcon(Player)` returns an `AssetManager` icon key, not a file path.
- Indicators are sorted deterministically by plugin name and icon key.

#### Dynamic tab overlays

Use `BasePluginOverlayWithTabs` for plugin overlays with tabs. New overlays
should prefer `setupTabContainer()` and `addTab(...)` instead of manual X
offsets. Add only currently available tabs; hidden tabs should simply not be
registered for that rebuild.

```java
public class YourOverlay extends BasePluginOverlayWithTabs {
    private PluginTab active = PluginTab.OVERVIEW;

    @Override
    protected void setupTabs() {
        setupTabContainer();
        addTab("Overview", 150, active == PluginTab.OVERVIEW,
                () -> switchTab(PluginTab.OVERVIEW));
        if (canShowAdminTab(uiPlayer)) {
            addTab("Admin", 150, active == PluginTab.ADMIN, true,
                    () -> switchTab(PluginTab.ADMIN));
        }
        setupActiveTabContent();
    }
}
```

Rules:

- Validate the active tab before rebuilding; if the active tab became hidden,
  switch to a visible default tab.
- Put tab-specific content in the overlay body, not in the tab container.
- Existing overlays can still use the older `tab(text, x, y, width, action)`
  helper until they migrate.

#### Plugin Info/Status providers

Use `PluginInfoStatusProviders` to expose plugin-specific RichText strings for
the shared Info/Status panel. Register one provider per plugin and wire plugin
buttons, menu entries, or commands to `PluginInfoStatusProviders.show(...)`.

```java
import de.omegazirkel.risingworld.tools.ui.PluginInfoStatusProvider;
import de.omegazirkel.risingworld.tools.ui.PluginInfoStatusProviders;

PluginInfoStatusProviders.registerProvider(new PluginInfoStatusProvider() {
    @Override
    public String getPluginName() {
        return YourPlugin.name;
    }

    @Override
    public String getInfo(Player player) {
        return "<b>Your Plugin</b>\nShort player-facing description.";
    }

    @Override
    public String getStatus(Player player) {
        return "Enabled: true";
    }
});

PluginInfoStatusProviders.show(player, YourPlugin.name);

// onDisable()
PluginInfoStatusProviders.unregisterProvider(YourPlugin.name);
```

Rules:

- `getInfo(Player)` and `getStatus(Player)` return RichText strings only.
- Return `""` or `null` for empty content; Tools renders both safely.
- Provider failures are caught by Tools and rendered as empty/error-safe panel
  content.

## AssetManager

... description coming soon ...

## Contributor Workflow

- Review `AGENTS.md`, `PLANS.md`, `.codex/agents.toml`, and `.codex/skills/` before making structural changes.
- Verify Rising World API usage with `scripts/verify-plugin-api.sh` when adding or changing API calls.
- Run `mvn -B -DskipTests package` and `mvn -B test` before release-facing changes are merged.
- Use `RUNTIME_TESTING.md` and `scripts/docker-runtime-smoke.sh <PluginFolderName>` for runtime smoke tests when behavior changes need server validation.
- Keep `README.md` and `HISTORY.md` current and use Conventional Commit titles for commits and PRs.
