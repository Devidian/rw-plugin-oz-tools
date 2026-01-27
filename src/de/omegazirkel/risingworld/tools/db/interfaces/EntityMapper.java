package de.omegazirkel.risingworld.tools.db.interfaces;

public interface EntityMapper<K, E> {

    K keyOf(E entity);
}

