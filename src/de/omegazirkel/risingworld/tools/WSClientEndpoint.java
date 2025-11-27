package de.omegazirkel.risingworld.tools;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

@ClientEndpoint
public class WSClientEndpoint {

	private static final Map<String, WSClientEndpoint> INSTANCES = new ConcurrentHashMap<>();

	private final URI endpointUri;

	private WebSocketHandler handler;
	private WebSocketContainer container;
	private ClassLoader extendedClassLoader;

	private static OZLogger logger() {
		return OZLogger.getInstance("OZ.Tools.WSCE");
	}

	public static void initLogger() {
		// this is a fix for logs redirected to Discord or other log files.
		logger();
	}

	private Session session;
	private final AtomicBoolean isConnected = new AtomicBoolean(false);
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
		connectAsync();
	}

	/** Connects without blocking the server thread */
	private void connectAsync() {
		CompletableFuture.runAsync(() -> {
			// Temporarily switch the ClassLoader for this thread
			// This allows ServiceLoader (used by ContainerProvider) to find Tyrus in the
			// /lib folder.
			ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
			try {
				if (extendedClassLoader == null)
					extendedClassLoader = getExtendedClassLoader(originalClassLoader);
				Thread.currentThread().setContextClassLoader(extendedClassLoader);

				container = ContainerProvider.getWebSocketContainer();
				if (container == null) {
					throw new IllegalStateException(
							"Could not find a WebSocketContainer implementation. Check if Tyrus JARs are in the /lib directory.");
				}
				container.setDefaultMaxSessionIdleTimeout(0);

				Session newSession = container.connectToServer(this, endpointUri);

				this.session = newSession;
				// onOpen will be called, which sets isConnected and logs the success

			} catch (Exception ex) {
				isConnected.set(false);
				logger().warn("❌ WebSocket connection failed: " + ex.getMessage());
				// Optional: Log the class loader hierarchy for debugging
				// logger().debug("ClassLoader used: " +
				// Thread.currentThread().getContextClassLoader().toString());
			} finally {
				// IMPORTANT: Always restore the original ClassLoader
				Thread.currentThread().setContextClassLoader(originalClassLoader);
			}
		});
	}

	/**
	 * Creates a new ClassLoader that includes all JARs from the 'lib' directory.
	 * 
	 * @param parent The parent classloader.
	 * @return A new URLClassLoader.
	 */
	private ClassLoader getExtendedClassLoader(ClassLoader parent) {
		try {
			File pluginJarFile = new File(
					WSClientEndpoint.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			File libDir = new File(pluginJarFile.getParentFile(), "lib");

			if (!libDir.exists() || !libDir.isDirectory()) {
				logger().warn("⚠️ 'lib' directory not found at: " + libDir.getAbsolutePath()
						+ ". Dependencies will not be loaded.");
				return parent; // Return original if no lib dir
			}

			File[] jarFiles = libDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
			List<URL> urls = new ArrayList<>();
			for (File jar : Objects.requireNonNull(jarFiles)) {
				urls.add(jar.toURI().toURL());
			}

			return new URLClassLoader(urls.toArray(new URL[0]), parent);
		} catch (Exception e) {
			logger().error("❌ Failed to create extended ClassLoader: " + e.getMessage());
			return parent; // Fallback to original
		}
	}

	// -----------------------------------------------------------------------------------
	// WebSocket lifecycle methods
	// -----------------------------------------------------------------------------------

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		isConnected.set(true);
		logger().info("🔌 WebSocket connection opened");
		if (handler != null)
			handler.onConnected(this);

	}

	@OnMessage
	public void onMessage(String message) {
		logger().info("📩 Received: " + message);
		// Forward to your tools logic if necessary
		if (handler != null)
			handler.onTextMessage(message);

	}

	@OnMessage
	public void onMessage(ByteBuffer buffer) {
		logger().info("📩 Received (binary) " + buffer.remaining() + " bytes");
		if (handler != null)
			handler.onBinaryMessage(buffer);

	}

	@OnError
	public void onError(Throwable t) {
		logger().warn("⚠️ WebSocket error: " + t.getMessage());
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		isConnected.set(false);
		this.session = null;
		logger().warn("🔌 WebSocket disconnected: " + reason);
		if (handler != null)
			handler.onDisconnected();

	}

	// -----------------------------------------------------------------------------------
	// Public API
	// -----------------------------------------------------------------------------------

	public boolean send(String msg) {
		ensureConnected(); // Ensure the connection logic is active
		if (!isConnected.get() || session == null || !session.isOpen())
			return false;
		session.getAsyncRemote().sendText(msg);
		return true;
	}

	public boolean send(ByteBuffer data) {
		ensureConnected(); // Ensure the connection logic is active
		if (!isConnected.get() || session == null || !session.isOpen())
			return false;
		session.getAsyncRemote().sendBinary(data);
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

		if (session != null) {
			try {
				session.close(new CloseReason(
						CloseReason.CloseCodes.NORMAL_CLOSURE,
						"Plugin shutdown"));
			} catch (Exception ignored) {
			}
		}

		// Shutdown the underlying Tyrus/Grizzly container
		if (container instanceof org.glassfish.tyrus.client.ClientManager) {
			try {
				((org.glassfish.tyrus.client.ClientManager) container).shutdown();
				logger().info("🔌 WebSocket container shut down.");
			} catch (Exception e) {
				logger().warn("⚠️ Error shutting down WebSocket container: " + e.getMessage());
			}
		}

		isConnected.set(false);
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
