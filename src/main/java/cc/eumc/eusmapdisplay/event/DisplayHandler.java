package cc.eumc.eusmapdisplay.event;

import org.bukkit.entity.Player;

public abstract class DisplayHandler {
    abstract void onCursorPositionChanged(Player player, int x, int y);

    abstract void onLeftClick(Player player, int x, int y);

    abstract void onRightClick(Player player, int x, int y);

    abstract void onWheelScroll(Player player, int wheelAmt);
}
