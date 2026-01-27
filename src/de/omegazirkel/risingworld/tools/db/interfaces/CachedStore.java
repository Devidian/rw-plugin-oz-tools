package de.omegazirkel.risingworld.tools.db.interfaces;

import java.util.Collection;

public interface CachedStore<K, E> {

    void loadAll();

    E get(K key);

    Collection<E> values();

    void put(K key, E entity);

    void remove(K key);

    void markDirty(E entity);

    void flush(boolean all);

    void clear();
}