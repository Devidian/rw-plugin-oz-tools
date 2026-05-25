package de.omegazirkel.risingworld.tools.ui;

import de.omegazirkel.risingworld.OZTools;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UIScrollView.ScrollViewMode;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Position;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;

public class PluginInfoStatusPanel extends OverlayBackPanel {
    private static final String PLAYER_ATTRIBUTE = "tools.ui.pluginInfoStatusPanel";
    private static final float PANEL_SIZE_PIXELS = 560f;
    private static final float CONTENT_TOP_PIXELS = 122f;
    private static final float CONTENT_HEIGHT_PIXELS = 398f;

    @FunctionalInterface
    public interface ContentFactory {
        UIElement create(Player player);
    }

    private enum Tab {
        INFO,
        STATUS
    }

    private static I18n t() {
        return I18n.getInstance(OZTools.name);
    }

    private final String pluginName;
    private final ContentFactory infoFactory;
    private final ContentFactory statusFactory;
    private Tab activeTab = Tab.INFO;

    public static void show(Player player, String pluginName, String infoText, String statusText) {
        show(player, pluginName, textFactory(infoText), textFactory(statusText));
    }

    public static void show(Player player, String pluginName, ContentFactory infoFactory, ContentFactory statusFactory) {
        if (player == null) {
            return;
        }
        remove(player);
        PluginInfoStatusPanel panel = new PluginInfoStatusPanel(player, pluginName, infoFactory, statusFactory);
        CursorManager.show(player);
        player.addUIElement(panel);
        player.setAttribute(PLAYER_ATTRIBUTE, panel);
    }

    public static void remove(Player player) {
        if (player == null) {
            return;
        }
        PluginInfoStatusPanel panel = (PluginInfoStatusPanel) player.getAttribute(PLAYER_ATTRIBUTE);
        if (panel != null) {
            player.removeUIElement(panel);
            player.deleteAttribute(PLAYER_ATTRIBUTE);
            CursorManager.hide(player);
        }
    }

    private static ContentFactory textFactory(String text) {
        return player -> textContent(text);
    }

    private static UIElement textContent(String text) {
        UILabel label = new UILabel(text == null ? "" : text);
        label.setRichTextEnabled(true);
        label.setPivot(Pivot.UpperLeft);
        label.style.position.set(Position.Absolute);
        label.style.left.set(0, Unit.Pixel);
        label.style.top.set(0, Unit.Pixel);
        label.style.width.set(100, Unit.Percent);
        label.style.minHeight.set(100, Unit.Percent);
        label.setFont(Font.Default);
        label.setFontSize(14);
        label.setFontColor(0xF4F0E6FF);
        label.setTextAlign(TextAnchor.UpperLeft);
        label.setTextWrap(true);
        return label;
    }

    private PluginInfoStatusPanel(Player player, String pluginName, ContentFactory infoFactory,
            ContentFactory statusFactory) {
        super(player);
        this.pluginName = pluginName == null ? "" : pluginName;
        this.infoFactory = infoFactory == null ? textFactory("") : infoFactory;
        this.statusFactory = statusFactory == null ? textFactory("") : statusFactory;
        setClickable(false);
        rebuild();
    }

    private void rebuild() {
        removeAllChilds();

        OZUIElement panel = new OZUIElement();
        panel.setPivot(Pivot.MiddleCenter);
        panel.setPosition(50, 50, true);
        panel.style.width.set(PANEL_SIZE_PIXELS, Unit.Pixel);
        panel.style.height.set(PANEL_SIZE_PIXELS, Unit.Pixel);
        panel.setBackgroundColor(0, 0, 0, 0.86f);
        panel.setBorderColor(0.95f, 0.75f, 0.25f, 0.6f);
        panel.setBorder(1);
        panel.setBorderEdgeRadius(6, false);
        addChild(panel);

        addHeader(panel);
        addTabs(panel);
        addContent(panel);
    }

    private void addHeader(OZUIElement panel) {
        UILabel title = new UILabel(t().get("TC_PLUGIN_INFO_STATUS_TITLE", uiPlayer)
                .replace("PH_PLUGIN_NAME", pluginName));
        title.setPivot(Pivot.UpperLeft);
        title.style.position.set(Position.Absolute);
        title.style.left.set(24, Unit.Pixel);
        title.style.top.set(18, Unit.Pixel);
        title.style.right.set(72, Unit.Pixel);
        title.style.height.set(34, Unit.Pixel);
        title.setFont(Font.DefaultBold);
        title.setFontSize(24);
        title.setFontColor(0xF4F0E6FF);
        title.setTextAlign(TextAnchor.MiddleLeft);
        title.setTextWrap(false);
        panel.addChild(title);

        OZUIElement closeButton = new OZUIElement();
        closeButton.setPivot(Pivot.UpperRight);
        closeButton.style.position.set(Position.Absolute);
        closeButton.style.right.set(10, Unit.Pixel);
        closeButton.style.top.set(18, Unit.Pixel);
        closeButton.setSize(34, 34, false);
        closeButton.setBorder(1);
        closeButton.setBorderColor(0.95f, 0.75f, 0.25f, 0.54f);
        closeButton.setHoverBorderColor(0xD7AE55FF);
        closeButton.setBorderEdgeRadius(4, false);
        closeButton.setBackgroundColor(0.12f, 0.10f, 0.08f, 0.9f);
        closeButton.setHoverBackgroundColor(0x611F1AF2);
        closeButton.setClickable(true);
        closeButton.setClickAction(event -> close());
        UILabel closeLabel = new UILabel("X");
        closeLabel.setPivot(Pivot.MiddleCenter);
        closeLabel.setPosition(50, 50, true);
        closeLabel.setSize(100, 100, true);
        closeLabel.setFont(Font.DefaultBold);
        closeLabel.setFontSize(18);
        closeLabel.setFontColor(0xF4F0E6FF);
        closeLabel.setTextAlign(TextAnchor.MiddleCenter);
        closeButton.addChild(closeLabel);
        panel.addChild(closeButton);
    }

    private void addTabs(OZUIElement panel) {
        panel.addChild(tab(t().get("TC_PLUGIN_INFO_STATUS_TAB_INFO", uiPlayer), 24, Tab.INFO));
        panel.addChild(tab(t().get("TC_PLUGIN_INFO_STATUS_TAB_STATUS", uiPlayer), 170, Tab.STATUS));
    }

    private OZUIElement tab(String label, float x, Tab tab) {
        boolean active = activeTab == tab;
        OZUIElement tabButton = new OZUIElement();
        tabButton.setPivot(Pivot.UpperLeft);
        tabButton.style.position.set(Position.Absolute);
        tabButton.style.left.set(x, Unit.Pixel);
        tabButton.style.top.set(76, Unit.Pixel);
        tabButton.setSize(134, 36, false);
        tabButton.setBackgroundColor(active ? 0x3A2D18D8 : 0x181713C8);
        tabButton.setHoverBackgroundColor(0x2A2419E8);
        tabButton.setBorder(1);
        tabButton.setBorderColor(active ? 0xD7AE55FF : 0x5E4A25FF);
        tabButton.setHoverBorderColor(0xD7AE55FF);
        tabButton.setBorderEdgeRadius(4, false);
        tabButton.setClickable(true);
        tabButton.setClickAction(event -> {
            activeTab = tab;
            rebuild();
        });

        UILabel tabLabel = new UILabel(label);
        tabLabel.setPivot(Pivot.MiddleCenter);
        tabLabel.setPosition(50, 50, true);
        tabLabel.setSize(100, 100, true);
        tabLabel.setFont(Font.DefaultBold);
        tabLabel.setFontSize(14);
        tabLabel.setFontColor(active ? 0xF2C766FF : 0xF4F0E6FF);
        tabLabel.setTextAlign(TextAnchor.MiddleCenter);
        tabButton.addChild(tabLabel);
        return tabButton;
    }

    private void addContent(OZUIElement panel) {
        UIScrollView scrollView = new UIScrollView(ScrollViewMode.Vertical);
        scrollView.setPivot(Pivot.UpperLeft);
        scrollView.style.position.set(Position.Absolute);
        scrollView.style.left.set(24, Unit.Pixel);
        scrollView.style.top.set(CONTENT_TOP_PIXELS, Unit.Pixel);
        scrollView.style.width.set(PANEL_SIZE_PIXELS - 48, Unit.Pixel);
        scrollView.style.height.set(CONTENT_HEIGHT_PIXELS, Unit.Pixel);
        scrollView.style.paddingTop.set(14);
        scrollView.style.paddingBottom.set(14);
        scrollView.style.paddingLeft.set(14);
        scrollView.style.paddingRight.set(14);
        scrollView.setBackgroundColor(0.08f, 0.08f, 0.08f, 0.55f);
        scrollView.setBorder(1);
        scrollView.setBorderColor(0.95f, 0.75f, 0.25f, 0.48f);
        scrollView.setBorderEdgeRadius(4, false);

        UIElement content = activeTab == Tab.INFO ? infoFactory.create(uiPlayer) : statusFactory.create(uiPlayer);
        if (content != null) {
            scrollView.addChild(content);
        }
        panel.addChild(scrollView);
    }

    private void close() {
        remove(uiPlayer);
    }
}
