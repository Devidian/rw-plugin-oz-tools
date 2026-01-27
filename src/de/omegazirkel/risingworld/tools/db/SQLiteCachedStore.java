package de.omegazirkel.risingworld.tools.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.omegazirkel.risingworld.tools.db.interfaces.CachedStore;
import de.omegazirkel.risingworld.tools.db.interfaces.DatabaseSchema;
import de.omegazirkel.risingworld.tools.db.interfaces.EntityMapper;
import de.omegazirkel.risingworld.tools.db.interfaces.SQLiteEntityMapper;
import de.omegazirkel.risingworld.tools.db.interfaces.StoreLifecycle;
import net.risingworld.api.Timer;

public abstract class SQLiteCachedStore<K, E> implements CachedStore<K, E>, StoreLifecycle {

    private final Map<K, E> cache = new ConcurrentHashMap<>();
    private final Set<E> dirty = ConcurrentHashMap.newKeySet();

    private final Timer flushTimer;
    private volatile boolean shutdown = false;

    private final EntityMapper<K, E> mapper;
    protected final SQLitePersistenceBackend<K, E> backend;

    protected SQLiteCachedStore(
            Connection connection,
            DatabaseSchema schema,
            SQLiteEntityMapper<K, E> mapper,
            float flushIntervalSeconds) throws SQLException {
        this.mapper = mapper;
        this.backend = new SQLitePersistenceBackend<>(connection, schema, mapper);
        loadAll();

        this.flushTimer = new Timer(
                flushIntervalSeconds,
                flushIntervalSeconds,
                -1,
                this::flushSafely);
        this.flushTimer.start();
    }

    @Override
    public void loadAll() {
        for (E entity : backend.loadAll()) {
            cache.put(mapper.keyOf(entity), entity);
        }
    }

    @Override
    public E get(K key) {
        return cache.get(key);
    }

    @Override
    public Collection<E> values() {
        return cache.values();
    }

    @Override
    public void put(K key, E entity) {
        cache.put(key, entity);
        dirty.add(entity);
    }

    @Override
    public void markDirty(E entity) {
        dirty.add(entity);
    }

    @Override
    public void flush(boolean all) {
        if (shutdown || (all ? cache.isEmpty() : dirty.isEmpty())) {
            return;
        }
        backend.saveAll(all ? cache.values() : dirty);
        dirty.clear();
    }

    public void flush() {
        flush(false);
    }

    private void flushSafely() {
        try {
            flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        cache.clear();
        dirty.clear();
    }

    @Override
    public void remove(K key) {
        E entity = cache.remove(key);
        if (entity != null) {
            dirty.remove(entity);
            backend.delete(entity);
        }
    }

    @Override
    public synchronized void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;

        flushTimer.kill();
        flush(true);
        cache.clear();
    }

}
