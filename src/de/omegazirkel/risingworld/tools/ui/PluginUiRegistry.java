package de.omegazirkel.risingworld.tools.ui;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** A provider registration owns one generation of its plugin label. */
final class PluginUiRegistry<T> {
    private final ConcurrentHashMap<String, Entry<T>> providers = new ConcurrentHashMap<>();

    Runnable register(String pluginLabel, T provider) {
        return register(pluginLabel, provider, null);
    }

    Runnable register(String pluginLabel, T provider, Consumer<Entry<?>> onRetired) {
        Entry<T> entry = new Entry<>(provider, onRetired);
        Entry<T> previous = providers.put(pluginLabel, entry);
        if (previous != null) previous.retire();
        return () -> {
            if (providers.remove(pluginLabel, entry)) entry.retire();
        };
    }

    Entry<T> entry(String pluginLabel) {
        return providers.get(pluginLabel);
    }

    T get(String pluginLabel) {
        Entry<T> entry = entry(pluginLabel);
        return entry == null ? null : entry.provider;
    }

    boolean containsKey(String pluginLabel) {
        return providers.containsKey(pluginLabel);
    }

    Set<String> keySet() {
        return providers.keySet();
    }

    static final class Entry<T> {
        final T provider;
        private final Consumer<Entry<?>> onRetired;
        private volatile boolean retired;

        private Entry(T provider, Consumer<Entry<?>> onRetired) {
            this.provider = provider;
            this.onRetired = onRetired;
        }

        boolean isRetired() {
            return retired;
        }

        private void retire() {
            retired = true;
            if (onRetired != null) onRetired.accept(this);
        }
    }
}
