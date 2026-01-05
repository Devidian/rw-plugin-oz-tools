# Changelog

## [unreleased]

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
