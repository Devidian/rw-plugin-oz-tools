package de.omegazirkel.risingworld.tools;

import java.nio.ByteBuffer;

/**
 * Interface for plugins that want to receive WebSocket messages.
 * Implement this in your GlobalIntercom plugin.
 */
public interface WebSocketHandler {

    /** Fired when a text message arrives */
    void onTextMessage(String message);

    /** Fired when a binary message arrives */
    default void onBinaryMessage(ByteBuffer buffer) {}

    /** Fired when WebSocket connects */
    default void onConnected(WSClientEndpoint wsce) {}

    /** Fired when WebSocket disconnects */
    default void onDisconnected() {}
}
