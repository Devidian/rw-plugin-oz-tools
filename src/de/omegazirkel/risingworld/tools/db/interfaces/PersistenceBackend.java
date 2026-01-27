package de.omegazirkel.risingworld.tools.db.interfaces;

import java.util.Collection;
import java.util.List;

public interface PersistenceBackend<E> {

    List<E> loadAll();

    void saveAll(Collection<E> entities);

    void delete(E entity);
}
