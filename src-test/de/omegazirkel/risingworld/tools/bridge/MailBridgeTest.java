package de.omegazirkel.risingworld.tools.bridge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MailBridgeTest {
    @Test
    public void attachmentRequestRequiresCompleteTrustedBoundaryData() {
        MailBridge.PluginAttachment attachment =
                new MailBridge.PluginAttachment("wood", 0, 2, 100, (short) 0, "", 0);
        MailBridge.PluginAttachmentMailRequest request = new MailBridge.PluginAttachmentMailRequest(
                "OZ - Marketplace", 42, "Buyer", "Subject", "Body", "wanted-1-2", List.of(attachment));

        assertTrue(attachment.valid());
        assertTrue(request.valid());
        assertFalse(new MailBridge.PluginAttachment("", 0, 2, 100, (short) 0, "", 0).valid());
        assertFalse(new MailBridge.PluginAttachmentMailRequest(
                "OZ - Marketplace", 42, "Buyer", "Subject", "Body", "", List.of(attachment)).valid());
    }

    @Test
    public void attachmentRequestDefensivelyCopiesCallerList() {
        List<MailBridge.PluginAttachment> attachments = new ArrayList<>();
        attachments.add(new MailBridge.PluginAttachment("wood", 0, 2, 100, (short) 0, "", 0));

        MailBridge.PluginAttachmentMailRequest request = new MailBridge.PluginAttachmentMailRequest(
                "OZ - Marketplace", 42, "Buyer", "Subject", "Body", "wanted-1-2", attachments);
        attachments.clear();

        assertEquals(1, request.attachments().size());
    }
}
