# Bugs 05 Thread Runtime Validation

## Objective
Finish runtime verification of the shared server-thread dispatch and PluginAPI
timer contracts introduced during the Bugs 05 crash-risk audit.

## Ownership
Owning repository/plugin: `rw-plugin-oz-tools`
Supporting repositories/plugins: all Tools consumers

## Dependencies
- Runtime: Rising World development server with PluginAPI `0.9.2`
- Build: Java 20 and Maven
- Optional integrations: Discord Connect and Global Intercom

## Risks
- PluginAPI `Timer` has no explicit public thread guarantee; verify it before
  treating timer callbacks as server-thread callbacks.

## Validation Strategy
- [x] `mvn -B clean test install -DskipTests=false`
- [x] Verify dispatcher lifecycle and exception isolation with unit tests
- [x] Runtime-assert `Plugin.isMainThread()` inside representative PluginAPI
  timer callbacks
- [ ] Exercise settings-file reload and plugin reload shutdown behavior

## Affected Repositories/Plugins
- `rw-plugin-oz-tools`
- Tools consumers using PluginAPI timers or file-change callbacks

## Rollback Considerations
Consumers can revert to direct PluginAPI `enqueue(...)`; do not restore direct
game API access from watcher or network callback threads.

## Implementation Checklist
- [x] Add lifecycle-bound dispatcher
- [x] Dispatch file-watcher and reload-debouncer callbacks
- [x] Keep WebSocket connect work on its lifecycle-owned scheduler
- [x] Add one-time PluginAPI Timer callback-context logging and safe dispatch
- [x] Document foreign-callback rules
- [x] Add focused dispatcher tests
- [ ] Complete development-server runtime validation
