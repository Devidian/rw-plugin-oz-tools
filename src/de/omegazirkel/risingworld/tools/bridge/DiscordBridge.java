package de.omegazirkel.risingworld.tools.bridge;

import java.lang.reflect.Method;

import net.risingworld.api.Plugin;

/** Optional, reflection-only bridge to OZ - Discord Connect. */
public class DiscordBridge {
    private final Plugin owner;

    public DiscordBridge(Plugin owner) {
        this.owner = owner;
    }

    public String getBotLanguage() {
        Plugin discord = getDiscordPlugin();
        if (discord == null) return "en";
        try {
            Object language = discord.getClass().getMethod("getBotLanguage").invoke(discord);
            if (language instanceof String value && !value.isBlank()) return value.trim();
        } catch (ReflectiveOperationException ex) {
            // Discord Connect remains optional.
        }
        return "en";
    }

    public boolean sendTextMessage(String message, long channelId) {
        return sendTextMessage(message, channelId, null);
    }

    public boolean sendTextMessage(String message, long channelId, byte[] image) {
        if (owner == null || channelId <= 0L || message == null || message.isBlank()) return false;
        Plugin discord = getDiscordPlugin();
        if (discord == null) return false;
        try {
            Method method = discord.getClass().getMethod("sendDiscordMessageToTextChannel", String.class,
                    long.class, byte[].class);
            method.invoke(discord, message, channelId, image);
            return true;
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    public boolean isAvailable() {
        return getDiscordPlugin() != null;
    }

    private Plugin getDiscordPlugin() {
        return owner == null ? null : owner.getPluginByName("OZ - Discord Connect");
    }
}
