package de.omegazirkel.risingworld.tools;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.omegazirkel.risingworld.OZTools;
import net.risingworld.api.Server;
import net.risingworld.api.events.general.HttpRequestEvent;

/** Outbound-only Manager connector. Credentials are kept outside PluginSettings. */
public final class GameConnectorService implements WebSocketHandler {
    private final OZTools plugin;
    private final PluginSettings settings;
    private final Path credentialFile;
    private final Path credentialKeyFile;
    private final byte[] credentialEncryptionKey;
    private final Set<String> featureEvents = ConcurrentHashMap.newKeySet();
    private final Set<String> acceptedFeatureEvents = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Set<Runnable>> featureReadyCallbacks = new ConcurrentHashMap<>();
    private WSClientEndpoint endpoint;
    private volatile String credential;

    public GameConnectorService(OZTools plugin, PluginSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.credentialFile = Path.of(plugin.getPath()).resolve("web.connector.credential");
        this.credentialKeyFile = credentialFile.resolveSibling("web.connector.key");
        this.credentialEncryptionKey = encryptionKey(credentialKeyFile, credentialFile);
    }

    public void start() {
        if (credentialEncryptionKey == null) {
            OZTools.logger().warn("Game connector disabled: local credential key could not be initialized");
            return;
        }
        credential = loadCredential();
        if (credential != null && !settings.webUseWebsockets) return;
        try {
            URI uri = URI.create(settings.webWsTargetUrl);
            if (!"wss".equalsIgnoreCase(uri.getScheme())) throw new IllegalArgumentException("web.wsTargetUrl must use wss");
            endpoint = WSClientEndpoint.getInstance(uri, this);
            endpoint.init();
        } catch (RuntimeException ex) {
            OZTools.logger().warn("Game connector disabled: " + ex.getMessage());
        }
    }

    @Override public void onConnected(WSClientEndpoint client) {
        acceptedFeatureEvents.clear();
        JsonObject message = new JsonObject();
        message.addProperty("schemaVersion", 1);
        if (credential == null) {
            message.addProperty("type", "connector.provision");
            // This is the game runtime's bound port, not an administrator setting.
            message.addProperty("gamePort", Server.getPort());
        } else {
            message.addProperty("type", "connector.authenticate");
            message.addProperty("credential", credential);
        }
        client.send(message.toString());
    }

    @Override public void onTextMessage(String text) {
        try {
            JsonObject message = JsonParser.parseString(text).getAsJsonObject();
            String type = message.has("type") ? message.get("type").getAsString() : "";
            if ("error".equals(type)) {
                String code = message.has("code") ? message.get("code").getAsString() : "unknown";
                if (!code.matches("[a-z_]{1,64}")) code = "unknown";
                OZTools.logger().warn("Game connector request rejected: " + code);
                return;
            }
            if ("connector.provisioned".equals(type) && message.has("credential")) {
                String receivedCredential = message.get("credential").getAsString();
                if (!storeCredential(receivedCredential)) return;
                credential = receivedCredential;
                if (!settings.webUseWebsockets && endpoint != null) endpoint.shutdown();
                return;
            }
            if ("connector.authenticated".equals(type) && settings.webUseWebsockets && endpoint != null) {
                OZTools.logger().info("Game connector authenticated; negotiating features");
                sendFeatures();
                return;
            }
            if ("connector.features.accepted".equals(type) && message.has("events")) {
                acceptedFeatureEvents.clear();
                for (JsonElement event : message.getAsJsonArray("events")) {
                    if (!event.isJsonPrimitive()) continue;
                    String eventName = event.getAsString();
                    if (!featureEvents.contains(eventName)) continue;
                    acceptedFeatureEvents.add(eventName);
                    Set<Runnable> callbacks = featureReadyCallbacks.get(eventName);
                    if (callbacks != null) {
                        for (Runnable callback : callbacks) callback.run();
                    }
                }
                OZTools.logger().info("Game connector features accepted: " + String.join(", ", acceptedFeatureEvents));
            }
        } catch (RuntimeException ex) {
            OZTools.logger().warn("Invalid game connector response");
        }
    }

    public void stop() { if (endpoint != null) endpoint.shutdown(); }

    /** Applies the shared native-route credential contract before DTO parsing. */
    public boolean authorizeRoute(HttpRequestEvent event) {
        if (settings.webAllowUnsecureRequests) return true;
        String header = event.getHeader("Authorization");
        String prefix = "Bearer ";
        String candidate = header != null && header.startsWith(prefix) ? header.substring(prefix.length()) : "";
        boolean authorized = credential != null && !candidate.isEmpty()
                && MessageDigest.isEqual(credential.getBytes(StandardCharsets.UTF_8), candidate.getBytes(StandardCharsets.UTF_8));
        if (authorized) return true;
        event.setResponseCode(401);
        event.setResponseHeader("WWW-Authenticate", "Bearer");
        event.setContentType("application/json; charset=utf-8");
        event.setResponseBody("{\"error\":\"unauthorized\"}");
        return false;
    }

    /**
     * Declares a supported connector event. The returned handle must be closed
     * by the owning plugin when that feature is no longer available.
     */
    public AutoCloseable registerFeature(String eventName) {
        return registerFeature(eventName, () -> { });
    }

    /**
     * Registers a feature and invokes {@code onReady} after each authenticated
     * feature negotiation. Callbacks run on the Tools server dispatcher.
     */
    public AutoCloseable registerFeature(String eventName, Runnable onReady) {
        if (eventName == null || !eventName.matches("[a-z][A-Za-z0-9]{0,63}")) {
            throw new IllegalArgumentException("Invalid game connector event name");
        }
        featureEvents.add(eventName);
        featureReadyCallbacks.computeIfAbsent(eventName, ignored -> ConcurrentHashMap.newKeySet()).add(onReady);
        sendFeatures();
        return () -> {
            Set<Runnable> callbacks = featureReadyCallbacks.get(eventName);
            if (callbacks != null) {
                callbacks.remove(onReady);
                if (callbacks.isEmpty()) {
                    featureReadyCallbacks.remove(eventName, callbacks);
                    featureEvents.remove(eventName);
                    acceptedFeatureEvents.remove(eventName);
                }
            }
            sendFeatures();
        };
    }

    /** Sends a bounded payload only after the backend accepted the declared feature. */
    public boolean publishFeatureEvent(String eventName, JsonElement data) {
        if (eventName == null || data == null || !featureEvents.contains(eventName)
                || !acceptedFeatureEvents.contains(eventName) || endpoint == null || !endpoint.isConnected()) {
            return false;
        }
        JsonObject message = new JsonObject();
        message.addProperty("type", "connector.event");
        message.addProperty("schemaVersion", 1);
        message.addProperty("event", eventName);
        message.add("data", data);
        return endpoint.send(message.toString());
    }

    private void sendFeatures() {
        if (!settings.webUseWebsockets || endpoint == null || !endpoint.isConnected()) return;
        JsonObject message = new JsonObject();
        message.addProperty("type", "connector.features");
        message.addProperty("schemaVersion", 1);
        com.google.gson.JsonArray events = new com.google.gson.JsonArray();
        featureEvents.stream().sorted().forEach(events::add);
        message.add("events", events);
        endpoint.send(message.toString());
    }

    private String loadCredential() {
        try {
            if (!Files.exists(credentialFile)) return null;
            String value = Files.readString(credentialFile, StandardCharsets.UTF_8).trim();
            if (value.isEmpty()) return null;
            String[] parts = value.split(":", -1);
            if (parts.length != 3 || !"v1".equals(parts[0])) return null;
            byte[] iv = java.util.Base64.getUrlDecoder().decode(parts[1]);
            byte[] encrypted = java.util.Base64.getUrlDecoder().decode(parts[2]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new javax.crypto.spec.SecretKeySpec(credentialEncryptionKey, "AES"),
                    new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8).trim();
        } catch (Exception ex) { return null; }
    }

    private boolean storeCredential(String value) {
        try {
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new javax.crypto.spec.SecretKeySpec(credentialEncryptionKey, "AES"),
                    new GCMParameterSpec(128, iv));
            String encoded = "v1:" + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(iv) + ":"
                    + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
            Path temporary = credentialFile.resolveSibling(credentialFile.getFileName() + ".tmp");
            Files.writeString(temporary, encoded, StandardCharsets.UTF_8);
            restrictPermissions(temporary);
            Files.move(temporary, credentialFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            restrictPermissions(credentialFile);
            return true;
        } catch (Exception ex) {
            OZTools.logger().error("Failed to persist game connector credential: " + ex.getMessage());
            return false;
        }
    }

    private static byte[] encryptionKey(Path keyFile, Path credentialFile) {
        String configured = System.getenv("OZ_TOOLS_GAME_CONNECTOR_CREDENTIAL_KEY");
        if (configured != null) {
            if (configured.length() < 32) return null;
            try {
                return MessageDigest.getInstance("SHA-256").digest(configured.getBytes(StandardCharsets.UTF_8));
            } catch (Exception ex) {
                return null;
            }
        }
        try {
            if (Files.exists(keyFile)) {
                byte[] key = Base64.getUrlDecoder().decode(Files.readString(keyFile, StandardCharsets.UTF_8).trim());
                return key.length == 32 ? key : null;
            }
            if (Files.exists(credentialFile)) {
                OZTools.logger().warn("Existing game connector credential needs its previous encryption key; keep "
                        + "OZ_TOOLS_GAME_CONNECTOR_CREDENTIAL_KEY configured or reset the connector credential");
                return null;
            }
            byte[] key = new byte[32];
            new SecureRandom().nextBytes(key);
            Path temporary = keyFile.resolveSibling(keyFile.getFileName() + ".tmp");
            Files.writeString(temporary, Base64.getUrlEncoder().withoutPadding().encodeToString(key), StandardCharsets.UTF_8);
            restrictPermissions(temporary);
            Files.move(temporary, keyFile, StandardCopyOption.ATOMIC_MOVE);
            restrictPermissions(keyFile);
            OZTools.logger().info("Created private local connector key");
            return key;
        } catch (Exception ex) {
            return null;
        }
    }

    private static void restrictPermissions(Path file) {
        try {
            Files.setPosixFilePermissions(file, EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
            // Windows does not implement POSIX file permissions.
        } catch (Exception ignored) {
            // The game process owner still controls the private plugin directory.
        }
    }
}
