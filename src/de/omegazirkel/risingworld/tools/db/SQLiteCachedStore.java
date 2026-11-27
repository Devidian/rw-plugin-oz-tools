package de.omegazirkel.risingworld.tools.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import de.omegazirkel.risingworld.tools.db.interfaces.CachedStore;
import de.omegazirkel.risingworld.tools.db.interfaces.DatabaseSchema;
import de.omegazirkel.risingworld.tools.db.interfaces.EntityMapper;
import de.omegazirkel.risingworld.tools.db.interfaces.SQLiteEntityMapper;
import de.omegazirkel.risingworld.tools.db.interfaces.StoreLifecycle;
import net.risingworld.api.Timer;

public abstract class SQLiteCachedStore<K, E> implements CachedStore<K, E>, StoreLifecycle {

    private final Map<K, E> cache = new ConcurrentHashMap<>();
    private final Map<K, E> dirty = new HashMap<>();

    private final Connection connection;
    private final Runnable stopTimer;
    private boolean timerStopped;
    private boolean shutdown;

    private final EntityMapper<K, E> mapper;
    protected final SQLitePersistenceBackend<K, E> backend;

    protected SQLiteCachedStore(
            Connection connection,
            DatabaseSchema schema,
            SQLiteEntityMapper<K, E> mapper,
            float flushIntervalSeconds) throws SQLException {
        this(connection, schema, mapper, flush -> {
            Timer timer = new Timer(flushIntervalSeconds, flushIntervalSeconds, -1, flush);
            timer.start();
            return timer::kill;
        });
    }

    SQLiteCachedStore(Connection connection, DatabaseSchema schema, SQLiteEntityMapper<K, E> mapper,
            Function<Runnable, Runnable> startTimer) throws SQLException {
        this.connection = connection;
        this.mapper = mapper;
        this.backend = new SQLitePersistenceBackend<>(connection, schema, mapper);
        loadAll();
        this.stopTimer = startTimer.apply(this::flushSafely);
    }

    @Override
    public void loadAll() {
        synchronized (connection) {
            for (E entity : backend.loadAll()) {
                cache.put(mapper.keyOf(entity), entity);
            }
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
        synchronized (connection) {
            cache.put(key, entity);
            dirty.put(key, entity);
        }
    }

    @Override
    public void markDirty(E entity) {
        synchronized (connection) {
            dirty.put(mapper.keyOf(entity), entity);
        }
    }

    @Override
    public void flush(boolean all) {
        synchronized (connection) {
            if (shutdown || (all ? cache.isEmpty() : dirty.isEmpty())) {
                return;
            }
            backend.saveAll(all ? cache.values() : dirty.values());
            dirty.clear();
        }
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
        synchronized (connection) {
            cache.clear();
            dirty.clear();
        }
    }

    @Override
    public void remove(K key) {
        synchronized (connection) {
            E entity = cache.get(key);
            if (entity != null) {
                backend.delete(entity);
                cache.remove(key);
                dirty.remove(key);
            }
        }
    }

    @Override
    public void shutdown() {
        synchronized (connection) {
            if (shutdown) {
                return;
            }
            if (!timerStopped) {
                stopTimer.run();
                timerStopped = true;
            }
            flush(true);
            shutdown = true;
            clear();
        }
    }

}
