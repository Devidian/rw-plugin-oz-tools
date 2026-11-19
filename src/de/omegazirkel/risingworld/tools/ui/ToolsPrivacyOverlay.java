package de.omegazirkel.risingworld.tools.ui;

import java.util.List;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.ExternalDataConsent;
import de.omegazirkel.risingworld.tools.ExternalDataConsent.Purpose;
import de.omegazirkel.risingworld.tools.ExternalDataConsent.Status;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UITarget;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;

/** Player decisions for the external data flows currently enabled on the server. */
public final class ToolsPrivacyOverlay extends BasePluginOverlayWithTabs {
    private static final String ATTRIBUTE = "tools.ui.privacy";
    private Purpose selectedPurpose;

    public static void show(Player player) {
        if (player.getAttribute(ATTRIBUTE) instanceof ToolsPrivacyOverlay previous) {
            player.removeUIElement(previous);
        }
        ToolsPrivacyOverlay overlay = new ToolsPrivacyOverlay(player);
        player.addUIElement(overlay, UITarget.Modal);
        player.setAttribute(ATTRIBUTE, overlay);
    }

    private ToolsPrivacyOverlay(Player player) {
        super(player, p -> p.deleteAttribute(ATTRIBUTE));
        rebuild();
    }

    @Override
    protected I18n t() {
        return I18n.getInstance(OZTools.name);
    }

    @Override
    protected String titleText() {
        return t().get("tc.privacy.title", uiPlayer);
    }

    @Override
    protected String descriptionText() {
        return t().get("tc.privacy.subtitle", uiPlayer);
    }

    @Override
    protected String legendText() {
        return "";
    }

    @Override
    protected void setupTabs() {
        List<Purpose> purposes = ExternalDataConsent.activePurposes();
        if (purposes.isEmpty()) {
            selectedPurpose = null;
            UILabel empty = new UILabel(t().get("tc.privacy.empty", uiPlayer));
            empty.setPivot(Pivot.UpperLeft);
            empty.setPosition(4, 5, true);
            empty.setSize(92, 20, true);
            empty.setTextAlign(TextAnchor.UpperLeft);
            empty.setTextWrap(true);
            body.addChild(empty);
            return;
        }

        if (selectedPurpose == null || !purposes.contains(selectedPurpose)) {
            selectedPurpose = purposes.stream()
                    .filter(purpose -> ExternalDataConsent.status(uiPlayer.getDbID(), purpose) == Status.PENDING)
                    .findFirst()
                    .orElse(purposes.getFirst());
        }

        setupTabContainer();
        for (Purpose purpose : purposes) {
            OZUIElement tab = addTab(ToolsExternalDataConsentPanel.purposeLabel(uiPlayer, purpose), 165,
                    purpose == selectedPurpose, () -> {
                        selectedPurpose = purpose;
                        rebuild();
                    });
            tab.style.maxWidth.set(100f / purposes.size(), Unit.Percent);
        }

        body.addChild(ToolsExternalDataConsentPanel.consentRow(uiPlayer, selectedPurpose, this::rebuild));
    }
}
