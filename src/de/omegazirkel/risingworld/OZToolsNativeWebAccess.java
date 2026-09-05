package de.omegazirkel.risingworld;

import net.risingworld.api.events.general.HttpRequestEvent;

/** Public native-web authorization API, deliberately outside the plugin listener hierarchy. */
public final class OZToolsNativeWebAccess {
    private OZToolsNativeWebAccess() {
    }

    public static boolean authorize(HttpRequestEvent event) {
        return OZToolsRuntime.authorizeNativeWebRequest(event);
    }
}
