package de.omegazirkel.risingworld.tools.bridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.risingworld.api.Plugin;

/** Optional, reflection-only bridge to OZ - Wallet. */
public class WalletBridge {
    private final Plugin owner;

    public WalletBridge(Plugin owner) {
        this.owner = owner;
    }

    public boolean isAvailable() {
        return wallet() != null;
    }

    public String defaultCurrencyIdentifier() {
        Object value = call("defaultCurrencyIdentifier");
        return value instanceof String text && !text.isBlank() ? text.trim() : "";
    }

    public WalletCallResult registerCurrency(String identifier, String name, String icon, String pluginIdentifier) {
        return result(call("registerCurrency", new Class<?>[] { String.class, String.class, String.class, String.class },
                identifier, name, icon, pluginIdentifier));
    }

    public WalletCallResult withdrawDefault(int playerDbId, long value, String reason, String pluginIdentifier) {
        return result(call("withdrawDefault", new Class<?>[] { int.class, long.class, String.class, String.class },
                playerDbId, value, reason, pluginIdentifier));
    }

    public WalletCallResult depositDefault(int playerDbId, long value, String reason, String pluginIdentifier) {
        return result(call("depositDefault", new Class<?>[] { int.class, long.class, String.class, String.class },
                playerDbId, value, reason, pluginIdentifier));
    }

    public WalletCallResult withdraw(int playerDbId, long value, String reason, String currencyIdentifier,
            String pluginIdentifier) {
        if (currencyIdentifier == null || currencyIdentifier.isBlank()) return withdrawDefault(playerDbId, value, reason, pluginIdentifier);
        return result(call("withdraw", new Class<?>[] { int.class, long.class, String.class, String.class, String.class },
                playerDbId, value, reason, currencyIdentifier, pluginIdentifier));
    }

    public WalletCallResult deposit(int playerDbId, long value, String reason, String currencyIdentifier,
            String pluginIdentifier) {
        if (currencyIdentifier == null || currencyIdentifier.isBlank()) return depositDefault(playerDbId, value, reason, pluginIdentifier);
        return result(call("deposit", new Class<?>[] { int.class, long.class, String.class, String.class, String.class },
                playerDbId, value, reason, currencyIdentifier, pluginIdentifier));
    }

    public WalletTransferCallResult transferIdempotent(int payerDbId, int payeeDbId, long value, String reason,
            String currencyIdentifier, String pluginIdentifier, String correlationId) {
        Object response = call("transferIdempotent",
                new Class<?>[] { int.class, int.class, long.class, String.class, String.class, String.class, String.class },
                payerDbId, payeeDbId, value, reason, currencyIdentifier, pluginIdentifier, correlationId);
        return new WalletTransferCallResult(Boolean.TRUE.equals(field(response, "success")), string(field(response, "errorCode")),
                string(field(response, "message")));
    }

    public long balanceDefault(int playerDbId) {
        return balanceValue(call("balanceDefault", new Class<?>[] { int.class }, playerDbId)).balance();
    }

    public BalanceInfo balance(int playerDbId, String currencyIdentifier) {
        Object response = currencyIdentifier == null || currencyIdentifier.isBlank()
                ? call("balanceDefault", new Class<?>[] { int.class }, playerDbId)
                : call("balance", new Class<?>[] { int.class, String.class }, playerDbId, currencyIdentifier);
        return balanceValue(response);
    }

    public List<CurrencyInfo> listCurrencies() {
        Object response = call("listCurrencies");
        if (!result(response).success() || !(field(response, "currencies") instanceof Iterable<?> currencies)) return List.of();
        List<CurrencyInfo> values = new ArrayList<>();
        for (Object currency : currencies) {
            Object identifier = getter(currency, "getIdentifier");
            if (!(identifier instanceof String text) || text.isBlank()) continue;
            values.add(new CurrencyInfo(text.trim().toUpperCase(Locale.ROOT), string(getter(currency, "getName")),
                    string(getter(currency, "getIconKey")), string(getter(currency, "getPluginIdentifier")),
                    getter(currency, "isDefaultCurrency") instanceof Boolean value && value));
        }
        return List.copyOf(values);
    }

    public List<String> currencyIdentifiers() {
        return listCurrencies().stream().map(CurrencyInfo::identifier).toList();
    }

    private BalanceInfo balanceValue(Object response) {
        if (!result(response).success()) return new BalanceInfo(false, 0L);
        Object value = getter(field(response, "balance"), "getBalance");
        return value instanceof Long amount ? new BalanceInfo(true, amount) : new BalanceInfo(false, 0L);
    }

    private Object call(String method) { return call(method, new Class<?>[0]); }
    private Object call(String method, Class<?>[] types, Object... values) {
        Plugin wallet = wallet();
        if (wallet == null) return null;
        try { return wallet.getClass().getMethod(method, types).invoke(wallet, values); }
        catch (ReflectiveOperationException ex) { return null; }
    }
    private Plugin wallet() { return owner == null ? null : owner.getPluginByName("OZ - Wallet"); }
    private static Object field(Object value, String name) {
        if (value == null) return null;
        try { Field field = value.getClass().getField(name); return field.get(value); }
        catch (ReflectiveOperationException ex) { return null; }
    }
    private static Object getter(Object value, String name) {
        if (value == null) return null;
        try { return value.getClass().getMethod(name).invoke(value); }
        catch (ReflectiveOperationException ex) { return null; }
    }
    private static String string(Object value) { return value instanceof String text ? text : ""; }
    private static WalletCallResult result(Object value) {
        return new WalletCallResult(Boolean.TRUE.equals(field(value, "success")), string(field(value, "message")));
    }

    public record WalletCallResult(boolean success, String message) {
        public static WalletCallResult success(String message) { return new WalletCallResult(true, message); }
    }
    public record WalletTransferCallResult(boolean success, String errorCode, String message) {}
    public record BalanceInfo(boolean success, long balance) {}
    public record CurrencyInfo(String identifier, String name, String iconKey, String pluginIdentifier, boolean defaultCurrency) {}
}
