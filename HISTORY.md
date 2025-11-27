# Changelog

## [unreleased]

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
