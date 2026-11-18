package de.omegazirkel.risingworld.tools.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.ExternalDataConsent;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.Server;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UITarget;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;

/** Admin-only, server-local table of declined external processing decisions. */
public final class DeclinedConsentAdminPanel extends BasePluginOverlay {
    private static final String ATTRIBUTE = "tools.ui.declinedConsentAdmin";
    private static final int PAGE_SIZE = 10;

    private final List<ExternalDataConsent.DeclinedPlayer> entries;
    private UIElement table;
    private UILabel pageLabel;
    private int page;

    public static void show(Player player) {
        if (player == null || !player.isAdmin()) return;
        if (player.getAttribute(ATTRIBUTE) instanceof UIElement old) player.removeUIElement(old);
        DeclinedConsentAdminPanel panel = new DeclinedConsentAdminPanel(player);
        player.addUIElement(panel, UITarget.Modal);
        player.setAttribute(ATTRIBUTE, panel);
    }

    private DeclinedConsentAdminPanel(Player player) {
        super(player, p -> p.deleteAttribute(ATTRIBUTE));
        entries = ExternalDataConsent.declinedPlayers().stream()
                .sorted(Comparator.comparingInt(ExternalDataConsent.DeclinedPlayer::dbId)
                        .thenComparing(ExternalDataConsent.DeclinedPlayer::purpose))
                .toList();
        rebuild();
        addTable();
        render();
    }

    @Override
    protected I18n t() { return I18n.getInstance(OZTools.name); }

    @Override
    protected String titleText() { return text("tc.privacy.admin.title"); }

    @Override
    protected String descriptionText() { return text("tc.privacy.admin.subtitle"); }

    @Override
    protected String legendText() { return text("tc.privacy.admin.legend"); }

    private void addTable() {
        body.addChild(label(text("tc.privacy.admin.dbid"), 3, 2, 19, 9, true));
        body.addChild(label(text("tc.privacy.admin.name"), 24, 2, 32, 9, true));
        body.addChild(label(text("tc.privacy.admin.purpose"), 59, 2, 38, 9, true));

        table = new UIElement();
        table.setPivot(Pivot.UpperLeft);
        table.setPosition(2, 13, true);
        table.setSize(96, 73, true);
        body.addChild(table);

        body.addChild(button(text("tc.menu.back"), 3, () -> changePage(-1)));
        pageLabel = label("", 38, 89, 24, 8, false);
        pageLabel.setTextAlign(TextAnchor.MiddleCenter);
        body.addChild(pageLabel);
        body.addChild(button(text("tc.privacy.admin.next"), 73, () -> changePage(1)));
    }

    private AdvancedButton button(String caption, int left, Runnable action) {
        AdvancedButton result = AdvancedButtonFactory.defaultButton(caption, event -> action.run());
        result.setPivot(Pivot.UpperLeft);
        result.setPosition(left, 89, true);
        result.setSize(23, 8, true);
        return result;
    }

    private void changePage(int delta) {
        page = Math.max(0, Math.min(Math.max(0, (entries.size() - 1) / PAGE_SIZE), page + delta));
        render();
    }

    private void render() {
        table.removeAllChilds();
        int first = page * PAGE_SIZE;
        int count = Math.min(PAGE_SIZE, entries.size() - first);
        if (count == 0) table.addChild(label(text("tc.privacy.admin.empty"), 2, 2, 96, 10, false));

        for (int i = 0; i < count; i++) {
            ExternalDataConsent.DeclinedPlayer entry = entries.get(first + i);
            UIElement row = new UIElement();
            row.setPivot(Pivot.UpperLeft);
            row.setPosition(0, i * 10, true);
            row.setSize(100, 10, true);
            row.setBackgroundColor(i % 2 == 0 ? 0x28241CCC : 0x171612CC);
            row.addChild(label("#" + entry.dbId(), 2, 0, 20, 100, false));
            String name = Server.getLastKnownPlayerName(entry.dbId());
            row.addChild(label(name == null || name.isBlank() ? "–" : name, 24, 0, 32, 100, false));
            row.addChild(label(text("tc.privacy.purpose." + entry.purpose().name().toLowerCase(Locale.ROOT)),
                    59, 0, 39, 100, false));
            table.addChild(row);
        }
        pageLabel.setText((page + 1) + " / " + Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE));
    }

    private UILabel label(String caption, int left, int top, int width, int height, boolean bold) {
        UILabel result = new UILabel(caption);
        result.setPivot(Pivot.UpperLeft);
        result.setPosition(left, top, true);
        result.setSize(width, height, true);
        result.setFont(bold ? Font.DefaultBold : Font.Default);
        result.setFontSize(15);
        result.setFontColor(0xF4F0E6FF);
        result.setTextAlign(TextAnchor.MiddleLeft);
        return result;
    }

    private String text(String key) { return t().get(key, uiPlayer); }

    @Override
    protected void close() {
        uiPlayer.deleteAttribute(ATTRIBUTE);
        super.close();
    }
}
