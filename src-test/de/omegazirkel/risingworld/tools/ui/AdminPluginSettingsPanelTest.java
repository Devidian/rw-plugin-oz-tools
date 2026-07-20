package de.omegazirkel.risingworld.tools.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.omegazirkel.risingworld.tools.settings.AdminSettingsEntry;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsType;

public class AdminPluginSettingsPanelTest {
    @Test
    public void acceptsFiniteDecimalValuesOnly() {
        AdminSettingsEntry entry = new AdminSettingsEntry("factor", "Factor", "", "1.0", "1.0",
                AdminSettingsType.DECIMAL, false, null);

        assertTrue(AdminPluginSettingsPanel.isValidValue(entry, "0.25"));
        assertTrue(AdminPluginSettingsPanel.isValidValue(entry, "-1.5e2"));
        assertFalse(AdminPluginSettingsPanel.isValidValue(entry, "not-a-number"));
        assertFalse(AdminPluginSettingsPanel.isValidValue(entry, "NaN"));
        assertFalse(AdminPluginSettingsPanel.isValidValue(entry, "Infinity"));
    }
}
