package de.omegazirkel.risingworld.tools.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class PluginUiRegistrationTest {
    @Test
    public void staleCloseCannotRemoveReplacementEvenWhenTheProviderObjectWasReused() {
        PluginUiRegistry<String> registry = new PluginUiRegistry<>();
        String provider = "same instance";
        Runnable stale = registry.register("A", provider);
        Runnable current = registry.register("A", provider);
        Runnable other = registry.register("B", "other");
        stale.run();
        assertSame(provider, registry.get("A"));
        current.run();
        current.run();
        assertNull(registry.get("A"));
        assertEquals("other", registry.get("B"));
        other.run();
        assertNull(registry.get("B"));
    }

    @Test
    public void onlyRetiredProviderGenerationsInvalidateTheirExistingPanels() {
        var registry = new PluginUiRegistry<String>();
        var retired = new ArrayList<PluginUiRegistry.Entry<?>>();
        Runnable closeFirst = registry.register("A", "same provider", retired::add);
        var first = registry.entry("A");
        Runnable closeSecond = registry.register("A", "same provider", retired::add);
        var second = registry.entry("A");
        Runnable closeOther = registry.register("B", "other provider", retired::add);
        var other = registry.entry("B");

        assertEquals(List.of(first), retired);
        assertTrue(first.isRetired());
        closeFirst.run();
        assertEquals(List.of(first), retired);
        assertSame(second, registry.entry("A"));
        assertFalse(second.isRetired());
        closeSecond.run();
        closeSecond.run();
        assertEquals(List.of(first, second), retired);
        assertTrue(second.isRetired());
        assertFalse(other.isRetired());
        closeOther.run();
    }

    @Test
    public void anAlreadyRenderedMenuCannotDispatchAClosedRegistration() {
        AtomicInteger opens = new AtomicInteger();
        var menu = new MenuItem("test-owner", "test-icon", "Test menu", ignored -> opens.incrementAndGet());
        Runnable old = PluginMenuManager.registerScopedPluginMenu(menu);
        MenuItem oldView = PluginMenuManager.sortedPluginMenuItems().stream()
                .filter(item -> "test-owner".equals(item.getPluginName())).findFirst().orElseThrow();
        Runnable current = PluginMenuManager.registerScopedPluginMenu(menu);
        try {
            old.run();
            oldView.getAction().onCall(null);
            assertEquals(0, opens.get());
            var remaining = PluginMenuManager.sortedPluginMenuItems().stream()
                    .filter(item -> "test-owner".equals(item.getPluginName())).toList();
            assertEquals(1, remaining.size());
            remaining.getFirst().getAction().onCall(null);
            assertEquals(1, opens.get());
        } finally {
            current.run();
        }
        assertEquals(0, PluginMenuManager.sortedPluginMenuItems().stream()
                .filter(item -> "test-owner".equals(item.getPluginName())).count());
    }
}
