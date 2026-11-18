package de.omegazirkel.risingworld.tools.ui;

import java.util.Locale;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.ExternalDataConsent;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;

/** Player consent controls, separate from ordinary OZ Tools preferences. */
public final class ToolsExternalDataConsentPanel extends BasePlayerPluginSettingsPanel {
    private final Player uiPlayer;

    public ToolsExternalDataConsentPanel(Player uiPlayer, String pluginLabel) {
        super(uiPlayer, pluginLabel);
        this.uiPlayer = uiPlayer;
    }

    @Override
    protected String headingText() {
        return text("tc.privacy.title");
    }

    @Override
    protected String descriptionText() {
        return text("tc.privacy.subtitle");
    }

    @Override
    protected void redrawContent() {
        flexWrapper.removeAllChilds();
        for (ExternalDataConsent.Purpose purpose : ExternalDataConsent.activePurposes()) {
            flexWrapper.addChild(consentRow(purpose));
        }
    }

    private OZUIElement consentRow(ExternalDataConsent.Purpose purpose) {
        ExternalDataConsent.Status status = ExternalDataConsent.status(uiPlayer.getDbID(), purpose);
        String purposeKey = purpose.name().toLowerCase(Locale.ROOT);
        String description = text("tc.privacy." + purposeKey);
        if (purpose == ExternalDataConsent.Purpose.MANAGER) {
            description = description.replace("PH_MANAGER_URL", OZTools.getSettings().webPublicManagerUrl);
        }

        OZUIElement row = defaultSettingsContainer();
        row.setMargin(0);
        row.style.marginBottom.set(12);
        row.style.width.set(100, Unit.Percent);
        row.style.height.set(190, Unit.Pixel);

        UILabel heading = label(text("tc.privacy.purpose." + purposeKey), 14, 28, true);
        heading.setFontSize(16);
        row.addChild(heading);

        UILabel details = label(description, 42, 60, false);
        details.setFontSize(13);
        row.addChild(details);

        UILabel decision = label(text("tc.privacy.statusLabel") + ": "
                + text("tc.privacy.status." + status.name().toLowerCase(Locale.ROOT)), 105, 24, true);
        decision.setFontSize(13);
        decision.setFontColor(switch (status) {
            case PENDING -> 0xD7AE55FF;
            case ACCEPTED -> 0x73D89CFF;
            case DECLINED -> 0xE88787FF;
        });
        row.addChild(decision);

        UIElement actions = new UIElement();
        actions.setPivot(Pivot.UpperCenter);
        actions.style.left.set(50, Unit.Percent);
        actions.style.top.set(139, Unit.Pixel);
        actions.setSize(346, 34, false);

        AdvancedButton reject = status == ExternalDataConsent.Status.DECLINED
                ? AdvancedButtonFactory.danger(text("tc.privacy.reject"), event -> reject(purpose))
                : AdvancedButtonFactory.defaultButton(text("tc.privacy.reject"), event -> reject(purpose));
        reject.setPivot(Pivot.UpperLeft);
        reject.setPosition(0, 0, false);
        reject.setSize(202, 34, false);

        AdvancedButton accept = status == ExternalDataConsent.Status.ACCEPTED
                ? AdvancedButtonFactory.ok(text("tc.privacy.accept"), event -> accept(purpose))
                : AdvancedButtonFactory.defaultButton(text("tc.privacy.accept"), event -> accept(purpose));
        accept.setPivot(Pivot.UpperLeft);
        accept.setPosition(214, 0, false);
        accept.setSize(132, 34, false);

        actions.addChild(reject);
        actions.addChild(accept);
        row.addChild(actions);

        return row;
    }

    private UILabel label(String value, int top, int height, boolean bold) {
        UILabel label = new UILabel(value);
        label.setPivot(Pivot.UpperLeft);
        label.setPosition(16, top, false);
        label.style.width.set(94, Unit.Percent);
        label.style.height.set(height, Unit.Pixel);
        label.setFont(bold ? Font.DefaultBold : Font.Default);
        label.setFontColor(0xF4F0E6FF);
        label.setTextAlign(TextAnchor.UpperLeft);
        label.setTextWrap(true);
        return label;
    }

    private void reject(ExternalDataConsent.Purpose purpose) {
        ExternalDataConsent.decide(uiPlayer, purpose, false);
        redrawContent();
    }

    private void accept(ExternalDataConsent.Purpose purpose) {
        ExternalDataConsent.decide(uiPlayer, purpose, true);
        redrawContent();
    }

    private String text(String key) {
        return I18n.getInstance(OZTools.name).get(key, uiPlayer);
    }
}
