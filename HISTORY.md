# Changelog

## [0.22.4] - 2026-07-20 | Update management completion

- fix: refresh installed version state immediately after a successful plugin update
- fix: restore the update action and show a localized warning when an update fails
- fix: use the maintained OZ release source when installed plugin metadata is missing
- change: show persistent release notes, targeted checks, and the most recent check time

## [0.22.3] - 2026-07-20 | Plugin update management

- feat: add opt-in delayed public GitHub update checks with OZ-only default trust policy
- feat: show current and available plugin versions in the shared settings overlay
- feat: add confirmed administrator update installation with staging and rollback
- fix: follow GitHub release asset redirects and serialize release checks with a configurable delay
- fix: keep the update action disabled while installation is in progress and report failed updates to administrators
- test: cover GitHub repository parsing, version comparisons, and release ZIP selection

## [0.22.2] - 2026-07-17 | Offline player lookup

- feat: add exact persisted-player name lookup for feature plugins that must address offline players

## [0.22.1] - 2026-07-14 | Icon set and shared UI polish

- fix: resolve player-selected shortcut icons in the inventory overlay
- fix: render the icon-style selector with the same settings-card layout as the other player options
- fix: place the global icon-style selector inside the Tools settings grid
- change: rename shared menu and status icon keys to their final semantic names
- change: expose the Modern/Classic icon selector only in OZTools settings
- change: own the shared debug and tools icons in OZTools

## [0.22.0] - 2026-06-15 | JDA shared runtime

- change: replace JavaCord with JDA 6.4.2 without voice/Opus/JNA native dependencies
- change: update HttpClient to 5.6.1, Gson to 2.14.0, Log4j to 2.26.0, and JUnit to 4.13.2
- feat: route JDA SLF4J 2.0.18 logging through Log4j
- fix: pin Kotlin 2.2.21 and clear packaged runtime libraries before every package build
- change: remove unused json-simple, HttpClient Fluent, Jakarta WebSocket, and Tyrus runtime libraries
- change: retain nv-websocket-client 2.14 for Global Intercom

## [0.21.2] - 2026-06-14 | Thread lifecycle diagnostics

- feat: add opt-in JVM-wide thread lifecycle sampling and summaries
- feat: add diagnostic lifecycle logging for Tools-owned threads

## [0.21.1] - 2026-06-13 | Callback thread safety

- feat: add lifecycle-bound `ServerThreadDispatcher` for foreign callback threads
- fix: dispatch file-watcher listeners and plugin reload scheduling onto the server thread
- fix: keep asynchronous WebSocket connect work on the lifecycle-owned scheduler
- feat: verify PluginAPI Timer callback context at runtime and dispatch indicator refreshes when required
- test: cover dispatcher enqueue, immediate execution, shutdown rejection, and exception isolation
- feat: let `BasePluginOverlay` subclasses provide dynamic header and footer text

## [0.21.0] - 2026-06-08 | Shared runtime and UI controls

- feat: add player-aware shortcut visibility
- fix: make admin-settings dropdown rows reserve open-list space and add multiline text setting support
- fix: prefer the player's current stable biome sample for region detection and ignore stone overlay samples
- change: remove the ineffective Escape key close hook while Rising World HUD-layer overlays still open the native menu first
- feat: add shared select/dropdown support for plugin admin settings
- change: load i18n resources once per plugin path during enable-time initialization
- change: remove deprecated `tools.db.SQLite` after all direct consumers migrated to `SQLiteConnectionFactory`
- change: enlarge shared inventory shortcut icons and render compact labels
- feat: add player setting to hide inventory shortcut labels
- fix: keep `/ozt` menu and inventory shortcut lists mutable after player visibility filtering

## [0.20.0] - 2026-05-26 | Player settings text wrapping

- fix: let default PlayerPluginSettings labels use card-relative width and a taller wrapped text area

## [0.19.0] - 2026-05-26 | Shared UI and plugin menu support

- change: render inventory shortcut buttons from the `/ozt` main menu actions and close inventory before launching them
- feat: register shared info/status and placeholder icons for plugin menus
- feat: render shared inventory plugin buttons as icon-only controls and add a Tools info/status inventory entry
- fix: refresh already-open inventory button panels when button registrations change
- fix: hide shared zone indicators while inventory is open and restore them when it closes
- feat: register OZ.Tools info/status content for the shared plugin panel
- feat: group OZ.Tools admin PluginSettings metadata with English and German labels
- refactor: keep OZ.Tools internal logging on the single main logger path
- docs: document shared UI integration rules for feature plugins
- feat: add shared admin settings group separator entries
- feat: filter shared admin integer setting inputs to numeric characters
- feat: add shared dynamic tab container helpers for plugin overlays
- feat: sort `/ozt` main radial menu plugin entries deterministically by plugin name
- feat: add public plugin info/status provider registration and launcher contract
- fix: let `BasePluginOverlay` subclasses decide when to render so tabbed overlays can initialize their own state first
- feat: add reusable centered plugin info/status panel with i18n title, close button, tabs, and scrollable content
- feat: add shared inventory overlay indicator provider registration and compact transparent HUD panel
- feat: add shared inventory overlay button registration and transparent flex panel
- feat: add admin-only `PluginSettings` tab registration for shared settings metadata
- feat: add simple boolean/integer/string settings editor support through plugin-provided writers
- feat: expose `SettingsFileEditor` helper for comment-preserving `settings.properties` value updates
- fix: let Tools settings fallback to `settings.default.properties` values when keys are missing

## [0.18.0] - 2026-05-18 | Shared plugin data UI and lookup helpers

- fix: restore colored one-line plugin welcome message
- fix: allow shared table scroll bodies to use pixel-accurate viewport heights
- fix: prevent shared table rows from overflowing horizontally inside scroll views
- fix: keep player plugin settings navigation and headers from overlapping their layout bounds
- fix: wrap long player plugin settings labels and draw complete switch-button borders
- feat: expose best-effort player record lookups from the shared players database helper

## [0.17.1] - 2026-05-10 | PluginAPI alignment and command help fix

- build: align bundled PluginAPI jar and Maven dependency version
- fix: correct dynamic command help text and area border bounds
- docs: correct installation path example

## [0.17.0] - 2026-03-11 | Players DB helper and logger cleanup

- docs: standardize agent prompts, PR checklist, and runtime smoke-test guidance
- build: add API verification helper and stricter CI/release validation flow
- feat: add `PlayerDatabaseHelper` for shared queries against the Rising World players database
- build: package only `README.md` and `HISTORY.md` into release artifacts
- refactor: route internal OZTools subsystems through the main `OZ.Tools` logger

- fix: mirror negative Y chunk position in `AreaUtils.chunksToArea`
- refactor: added setting `logInternal` (default false)
  - will disable seperated logfiles for plugins if `true`

## [0.16.0] - 2026-02-04 | AreaUtils and removed sleep announcement

- feat: added AreaUtils class for some shared area methods (from LandClaim)
- refactor: moved sleep announcement to AdminUtils plugin

## [0.15.0] - 2026-01-27 | Refactor SQLite access

- feat: new SQLite handling implemented using cached values
  - reduces reads/writes to a minimum
- refactor: SQLite class marked as deprecated, will be removed in a future release

## [0.14.0] - 2026-01-05 | Player-Plugin-Settings Manager

- feat: new API for Plugin Settings, accessible through main radial menu

## [0.13.0] - 2025-12-28 | Refactor Consumer->Callback

[BREAKING]

- refactor: replaced Consumer with Callback (java -> rw api)

## [0.12.0] - 2025-12-24 | PlayerSettings

- feat: `PlayerSettings` database to persist player-plugin-settings [0.12.0]
- feat: small accouncement feature for players going to bed (default off) [0.11.2]
- refactor: moved onClick from `BaseButton` to `OZUIElement` [0.11.1]
- refactor: streamlined welcome message property [0.11.2]
- refactor: fix invisible line chat spam [0.11.3]
- refactor: wrong player language for bed message [0.11.4]

## [0.11.0] - 2025-12-18 | OZ UI

- feat: introduce OZ UI
  - buttons and Menu items for plugins
  - CursorManager: manages if cursor should be visible or not (when opening child-windows and closing them)
  - PluginMenuManager: main menu leads to other plugin menues
- fix,CRITICAL: create one I18n for each Plugin (translations getting overridden issue)

## [0.10.0] - 2025-12-08 | Settings & I18n refactoring

- refactor: Moved settings to PluginSettings.java (sync codebase with other plugins)
- refactor,BREAKING: I18n is now a Singleton class
- refactor: `OZLogger.setLevel` now returns `this`
- refactor: logLevel is now string and uses values (names) from log4J
  - see `settings.default.properties` for possible values (0.9.5)
- refactor: name of SQLite logger changed (0.9.5)
- refactor: dont ship `settings.properties`, renamed to `settings.default.properties` (0.9.1)
  - prevent override on plugin update
- ci: activated dependency-prs workflow (0.9.2)

## [0.9.0] - 2025-11-26 | AI-Refactoring

Note: To get this plugin up to date, i've used AI (Gemini/Chat-GPT) to help me fixing some critical issues.

- feat: GitHub Action for maven
  - needs PluginAPI in ./libs/
- refactor: Project is now a real Plugin again
- refactor: now includes all libs
- refactor: now using Log4J for OZLogger
- refactor: FileWatcher now watches globally
  - no need to implement in other plugins
- refactor: better implementation for WS handling
- fix: removed non-daemon threads preventing RW from restart/shutdown

## [0.8.1] - 2025-11-14

- refactor: changed color codes to new format

## [0.8.0] - 2025-11-13

- refactor: updated and build for unity
  - removed rwgui
  - removed assets (rwgui assets)

## [0.7.0] - 2019-12-10

- feat: `gson` maven dependency added
- feat: `javax.websocket-api` maven dependency added
- feat: `tyrus-standalone-client` maven dependency added
- feat: `WSClientEndpoint` helper class (moved from GlobalIntercom)
- feat: `SQLite.getRawDatabase` to get raw Database object
- feat: README file
- fix: check if db is not null on `SQLite`

### Changed

- [breaking] `PluginChangeWatcher` is now static with only one `WatchService` and one `WatchThread` for all `FileChangeListener` resulting in less threads opened.

## [0.6.0] - 2019-04-13

- feat: Merged Miwarre's rwgui plugin into this lib as drop-in-replacement

## [0.5.0] - 2019-04-06

- feat: Wrapper class for SQLite Database connection

## [0.4.1] - 2019-03-05

- fix: Colors class getInstance was not static

## [0.4.0] - 2019-01-31

- feat: Colors singleton class to provide streamlined colors for different plugins

## [0.3.0] - 2019-01-28

- feat: new method `getLanguageAvailable` shows all language files loaded
- refactor: the method `getLanguageUsed` now adds a hint when default language is choosen

## [0.2.0] - 2019-01-25

- feat: method to get the effective language I18n->getLanguageUsed(String language)
- HISTORY.en.md

## [0.1.1]

## [0.1.0]
