/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.omegazirkel.risingworld.tools;

import java.nio.file.Path;

/**
 *
 * @author Maik
 */
public interface FileChangeListener {
    default void onJarChanged(Path jarPath) {
    }

    default void onSettingsChanged(Path settingsPath) {
    }

    default void onOtherFileChanged(Path filePath) {
    }
}
