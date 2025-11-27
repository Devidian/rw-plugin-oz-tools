/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.omegazirkel.risingworld.tools;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class PluginFileWatcher implements AutoCloseable {
    private final WatchService watchService;
    private final Map<WatchKey, Path> keyToPath = new HashMap<>();
    private final List<FileChangeListener> listeners = new ArrayList<>();
    private final Map<Path, FileChangeListener> settingsFiles = new HashMap<>();
    private final PluginReloadDebouncer jarDebouncer;

    private static OZLogger logger() {
        return OZLogger.getInstance("OZ.Tools.PluginFileWatcher");
    }

    private final ExecutorService watcherThread = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "PluginFileWatcher-Thread");
            t.setDaemon(true);
            return t;
        }
    });

    public PluginFileWatcher(Path rootDir, PluginReloadDebouncer jarDebouncer) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.jarDebouncer = jarDebouncer;

        // recursive register all directories
        registerAll(rootDir);

        // start watch-service loop
        watcherThread.submit(this::processEvents);
    }

    public void addListener(FileChangeListener listener) {
        listeners.add(listener);
    }

    public void addSettingsFile(Path path, FileChangeListener listener) {
        settingsFiles.put(path.toAbsolutePath(), listener);
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
        keyToPath.put(key, dir);
    }

    private void processEvents() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                Path dir = keyToPath.get(key);
                if (dir == null) {
                    key.reset();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // handle overflow
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    // if directory was registered re-register recursive
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        if (Files.isDirectory(child)) {
                            registerAll(child);
                        }
                    }

                    // handle by kind
                    handleFileEvent(kind, child);
                }

                boolean valid = key.reset();
                if (!valid) {
                    keyToPath.remove(key);
                    if (keyToPath.isEmpty()) {
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            logger().fatal("InterruptedException: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger().fatal("IOException: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                watchService.close();
            } catch (IOException io) {
                logger().fatal("IOException (close):" + io.getMessage());
                io.printStackTrace();
            }
        }
    }

    private void handleFileEvent(WatchEvent.Kind<?> kind, Path path) {
        String filename = path.getFileName().toString().toLowerCase();

        // check if jar
        if (filename.endsWith(".jar")) {
            jarDebouncer.jarChanged(path);
            // notify other listeners for Jar-change
            for (FileChangeListener l : listeners) {
                try {
                    l.onJarChanged(path);
                } catch (Exception e) {
                    logger().fatal("onJarChanged: " + e.getMessage());
                }
            }
        }
        // check for settings.properties
        else if (filename.equals("settings.properties")) {
            FileChangeListener listener = settingsFiles.get(path.toAbsolutePath());

            if (listener != null) {
                listener.onSettingsChanged(path);
            } else {
                // falls Settings-Datei nicht registriert ist → ignorieren
                logger().info("ℹ️ Unknown settings.properties changed: " + path);
            }
        }
        // other files
        else {
            for (FileChangeListener l : listeners) {
                try {
                    l.onOtherFileChanged(path);
                } catch (Exception e) {
                    logger().fatal("onOtherFileChanged: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void close() {
        watcherThread.shutdownNow();
        try {
            watchService.close();
        } catch (IOException io) {
            logger().fatal("IOException (close):" + io.getMessage());
        }
    }
}
