package de.omegazirkel.risingworld.tools;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import de.omegazirkel.risingworld.OZTools;

public class WSClientEndpoint {

	private static final Map<String, WSClientEndpoint> INSTANCES = new ConcurrentHashMap<>();

	private final URI endpointUri;

	private WebSocketHandler handler;

	private static OZLogger logger() {
		return OZTools.logger();
	}

	private WebSocket socket;
	private final AtomicBoolean isConnected = new AtomicBoolean(false);
	private final AtomicBoolean isConnecting = new AtomicBoolean(false);
	private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
	private final AtomicBoolean isReconnectLoopActive = new AtomicBoolean(false);

	// Executor for reconnect attempts + async connect
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread th = new Thread(r, "WebSocket-Reconnect");
		th.setDaemon(true);
		return th;
	});

	public WSClientEndpoint(URI uri) {
		this.endpointUri = uri;
		// The reconnect loop is now started on the first call to ensureConnected()
	}

	public WSClientEndpoint(String uri) {
		this(URI.create(uri));
	}

	public WSClientEndpoint(URI uri, WebSocketHandler handler) {
		this(uri);
		this.handler = handler;
	}

	public WSClientEndpoint(String uri, WebSocketHandler handler) {
		this(URI.create(uri), handler);
	}

	public WSClientEndpoint setHandler(WebSocketHandler handler) {
		this.handler = handler;
		return this;
	}

	public static WSClientEndpoint getInstance(String uri) {
		return getInstance(URI.create(uri));
	}

	public static WSClientEndpoint getInstance(String uri, WebSocketHandler handler) {
		return getInstance(URI.create(uri), handler);
	}

	public static WSClientEndpoint getInstance(URI uri, WebSocketHandler handler) {
		return getInstance(uri).setHandler(handler);
	}

	public static WSClientEndpoint getInstance(URI uri) {
		String key = uri.toString();

		if (!INSTANCES.containsKey(key)) {
			INSTANCES.put(key, new WSClientEndpoint(uri));
		}
		return INSTANCES.get(key);
	}

	/** Ensures connection is active, tries async connect if not */
	private void ensureConnected() {
		if (isShuttingDown.get())
			return;

		// Start the reconnect loop only once on the first call
		if (isReconnectLoopActive.compareAndSet(false, true)) {
			scheduler.scheduleAtFixedRate(this::reconnectTask, 0, 60, TimeUnit.SECONDS);
		}

		if (isConnected.get())
			return;

		logger().info("[WebSocket] Attempting connection to " + endpointUri);
		connectAsync();
	}

	/** The actual task that is scheduled to run periodically. */
	private void reconnectTask() {
		if (isShuttingDown.get() || isConnected.get()) {
			return;
		}
		logger().debug("[WebSocket] Reconnecting to " + endpointUri);
		connectAsync();
	}

	/** Connects without blocking the server thread */
	private void connectAsync() {
		if (!isConnecting.compareAndSet(false, true)) {
			logger().debug("[WebSocket] connect already in progress for " + endpointUri);
			return;
		}
		logger().debug("[WebSocket] connectAsync() to " + endpointUri);
		CompletableFuture.runAsync(() -> {
			try {
				WebSocket newSocket = new WebSocketFactory()
						.setConnectionTimeout(10000)
						.setSocketTimeout(0)
						.createSocket(endpointUri);
				newSocket.addListener(new WebSocketAdapter() {
					@Override
					public void onConnected(WebSocket websocket, Map<String, java.util.List<String>> headers) {
						socket = websocket;
						isConnected.set(true);
						logger().info("🔌 WebSocket connection opened");
						if (handler != null)
							handler.onConnected(WSClientEndpoint.this);
					}

					@Override
					public void onTextMessage(WebSocket websocket, String message) {
						logger().info("📩 Received: " + message);
						if (handler != null)
							handler.onTextMessage(message);
					}

					@Override
					public void onBinaryMessage(WebSocket websocket, byte[] binary) {
						logger().info("📩 Received (binary) " + binary.length + " bytes");
						if (handler != null)
							handler.onBinaryMessage(ByteBuffer.wrap(binary));
					}

					@Override
					public void onConnectError(WebSocket websocket, WebSocketException exception) {
						isConnected.set(false);
						logger().warn("❌ WebSocket connection failed: " + exception.getMessage());
					}

					@Override
					public void onError(WebSocket websocket, WebSocketException exception) {
						logger().warn("⚠️ WebSocket error: " + exception.getMessage());
					}

					@Override
					public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
							WebSocketFrame clientCloseFrame, boolean closedByServer) {
						isConnected.set(false);
						if (socket == websocket)
							socket = null;
						logger().warn("🔌 WebSocket disconnected");
						if (handler != null)
							handler.onDisconnected();
					}
				});

				socket = newSocket;
				newSocket.connect();
				logger().debug("[WebSocket] Session connected to " + endpointUri);

			} catch (Exception ex) {
				isConnected.set(false);
				logger().warn("❌ WebSocket connection failed: " + ex.getMessage());
			} finally {
				isConnecting.set(false);
			}
		});
	}

	// -----------------------------------------------------------------------------------
	// Public API
	// -----------------------------------------------------------------------------------

	public void init() {
		ensureConnected();
	}

	public boolean send(String msg) {
		ensureConnected(); // Ensure the connection logic is active
		if (!isConnected.get() || socket == null || !socket.isOpen())
			return false;
		socket.sendText(msg);
		return true;
	}

	public boolean send(ByteBuffer data) {
		ensureConnected(); // Ensure the connection logic is active
		if (!isConnected.get() || socket == null || !socket.isOpen())
			return false;
		byte[] bytes = new byte[data.remaining()];
		data.slice().get(bytes);
		socket.sendBinary(bytes);
		return true;
	}

	public boolean isConnected() {
		return isConnected.get();
	}

	/** Clean shutdown for onDisable() */
	public void shutdown() {
		logger().info("🛑 Shutting down WebSocket");

		isShuttingDown.set(true);

		scheduler.shutdownNow();

		if (socket != null) {
			try {
				socket.disconnect();
			} catch (Exception ignored) {
			}
		}

		isConnected.set(false);
		isConnecting.set(false);
	}

	/** Cleanly shuts down all managed WebSocket client instances. */
	public static void shutdownAll() {
		logger().info("🛑 Shutting down all WebSocket clients...");
		for (WSClientEndpoint client : INSTANCES.values()) {
			client.shutdown();
		}
		INSTANCES.clear();
		logger().info("✅ All WebSocket clients have been shut down.");
	}
}
