package de.omegazirkel.risingworld.tools.db.interfaces;

public interface StoreLifecycle {

    void loadAll();

    void flush(boolean all);

    void shutdown();
}
