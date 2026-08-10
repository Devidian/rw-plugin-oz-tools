package de.omegazirkel.risingworld.tools.bridge;

import java.lang.reflect.Method;
import java.util.List;

import net.risingworld.api.Plugin;

/** Optional consumer-side reflection bridge for trusted OZ Mail delivery. */
public class MailBridge {
    public static final int API_VERSION = 2;
    private final Plugin owner;

    public MailBridge(Plugin owner) {
        this.owner = owner;
    }

    public BridgeResult sendTextMail(PluginMailRequest request) {
        if (owner == null || request == null || !request.valid()) return BridgeResult.invalid();
        String callerPlugin = owner.getDescription("name");
        if (callerPlugin == null || callerPlugin.isBlank()
                || !callerPlugin.trim().equalsIgnoreCase(request.senderPlugin().trim())) return BridgeResult.invalid();
        Plugin mailPlugin = owner.getPluginByName("OZ - Mail");
        if (mailPlugin == null) return BridgeResult.unavailable();
        try {
            Method method = mailPlugin.getClass().getMethod("sendPluginMail", String.class, int.class,
                    String.class, String.class, String.class, String.class);
            Object result = method.invoke(mailPlugin, callerPlugin.trim(), request.recipientDbId(),
                    request.recipientName(), request.subject(), request.body(), request.correlationId());
            return BridgeResult.from(result);
        } catch (ReflectiveOperationException ex) {
            return BridgeResult.unavailable();
        }
    }

    /** Sends a trusted, idempotent operator report without consuming mailbox capacity. */
    public BridgeResult sendSystemReport(PluginMailRequest request) {
        if (owner == null || request == null || !request.valid()) return BridgeResult.invalid();
        String callerPlugin = owner.getDescription("name");
        if (callerPlugin == null || callerPlugin.isBlank()
                || !callerPlugin.trim().equalsIgnoreCase(request.senderPlugin().trim())) return BridgeResult.invalid();
        Plugin mailPlugin = owner.getPluginByName("OZ - Mail");
        if (mailPlugin == null) return BridgeResult.unavailable();
        try {
            Method method = mailPlugin.getClass().getMethod("sendPluginSystemReport", String.class, int.class,
                    String.class, String.class, String.class, String.class);
            Object result = method.invoke(mailPlugin, callerPlugin.trim(), request.recipientDbId(),
                    request.recipientName(), request.subject(), request.body(), request.correlationId());
            return BridgeResult.from(result);
        } catch (ReflectiveOperationException ex) {
            return BridgeResult.unavailable();
        }
    }

    public boolean canReceiveMail(int recipientDbId) {
        if (owner == null || recipientDbId <= 0) return false;
        Plugin mailPlugin = owner.getPluginByName("OZ - Mail");
        if (mailPlugin == null) return false;
        try {
            Method method = mailPlugin.getClass().getMethod("canReceivePluginMail", int.class);
            return Boolean.TRUE.equals(method.invoke(mailPlugin, recipientDbId));
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    public BridgeResult sendAttachmentMail(PluginAttachmentMailRequest request) {
        if (owner == null || request == null || !request.valid()) return BridgeResult.invalid();
        String callerPlugin = owner.getDescription("name");
        if (callerPlugin == null || callerPlugin.isBlank()
                || !callerPlugin.trim().equalsIgnoreCase(request.senderPlugin().trim())) return BridgeResult.invalid();
        Plugin mailPlugin = owner.getPluginByName("OZ - Mail");
        if (mailPlugin == null) return BridgeResult.unavailable();
        List<PluginAttachment> attachments = request.attachments();
        String[] itemNames = new String[attachments.size()];
        int[] variants = new int[attachments.size()];
        int[] amounts = new int[attachments.size()];
        int[] durabilities = new int[attachments.size()];
        short[] statuses = new short[attachments.size()];
        String[] modifiers = new String[attachments.size()];
        int[] colors = new int[attachments.size()];
        for (int i = 0; i < attachments.size(); i++) {
            PluginAttachment attachment = attachments.get(i);
            itemNames[i] = attachment.itemName();
            variants[i] = attachment.variant();
            amounts[i] = attachment.amount();
            durabilities[i] = attachment.durability();
            statuses[i] = attachment.status();
            modifiers[i] = attachment.modifier();
            colors[i] = attachment.color();
        }
        try {
            Method method = mailPlugin.getClass().getMethod("sendPluginMailWithAttachments", String.class, int.class,
                    String.class, String.class, String.class, String.class, String[].class, int[].class, int[].class,
                    int[].class, short[].class, String[].class, int[].class);
            Object result = method.invoke(mailPlugin, callerPlugin.trim(), request.recipientDbId(),
                    request.recipientName(), request.subject(), request.body(), request.correlationId(), itemNames,
                    variants, amounts, durabilities, statuses, modifiers, colors);
            return BridgeResult.from(result);
        } catch (ReflectiveOperationException ex) {
            return BridgeResult.unavailable();
        }
    }

    public record PluginMailRequest(String senderPlugin, int recipientDbId, String recipientName, String subject,
            String body, String correlationId) {
        public boolean valid() {
            return senderPlugin != null && !senderPlugin.isBlank() && recipientDbId > 0
                    && recipientName != null && !recipientName.isBlank() && subject != null && !subject.isBlank()
                    && body != null;
        }
    }

    public record PluginAttachment(String itemName, int variant, int amount, int durability, short status,
            String modifier, int color) {
        public boolean valid() {
            return itemName != null && !itemName.isBlank() && variant >= 0 && amount > 0
                    && modifier != null;
        }
    }

    public record PluginAttachmentMailRequest(String senderPlugin, int recipientDbId, String recipientName,
            String subject, String body, String correlationId, List<PluginAttachment> attachments) {
        public PluginAttachmentMailRequest {
            attachments = attachments == null ? List.of() : List.copyOf(attachments);
        }

        public boolean valid() {
            return senderPlugin != null && !senderPlugin.isBlank() && recipientDbId > 0
                    && recipientName != null && !recipientName.isBlank() && subject != null && !subject.isBlank()
                    && body != null && correlationId != null && !correlationId.isBlank() && !attachments.isEmpty()
                    && attachments.stream().allMatch(PluginAttachment::valid);
        }
    }

    public record BridgeResult(String code, boolean success, boolean reconciliationRequired, String mailId,
            String correlationId) {
        static BridgeResult unavailable() { return new BridgeResult("MAIL_UNAVAILABLE", false, false, "", ""); }
        static BridgeResult invalid() { return new BridgeResult("INVALID_REQUEST", false, false, "", ""); }
        static BridgeResult from(Object result) {
            return new BridgeResult(string(result, "code"), bool(result, "success"),
                    bool(result, "reconciliationRequired"), string(result, "mailId"), string(result, "correlationId"));
        }
        private static String string(Object target, String method) { Object value = invoke(target, method); return value == null ? "" : String.valueOf(value); }
        private static boolean bool(Object target, String method) { return Boolean.TRUE.equals(invoke(target, method)); }
        private static Object invoke(Object target, String method) {
            if (target == null) return null;
            try { return target.getClass().getMethod(method).invoke(target); }
            catch (ReflectiveOperationException ex) { return null; }
        }
    }
}
