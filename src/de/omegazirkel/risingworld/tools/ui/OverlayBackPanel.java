package de.omegazirkel.risingworld.tools.ui;

import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.style.Pivot;

public abstract class OverlayBackPanel extends OZUIElement {

    protected Player uiPlayer = null;

    public OverlayBackPanel(Player uiPlayer) {
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
        this.setBackgroundColor(0, 0, 0, 0.4f);
        this.uiPlayer = uiPlayer;
    }
}
