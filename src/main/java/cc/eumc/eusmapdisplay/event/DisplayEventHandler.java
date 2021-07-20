package cc.eumc.eusmapdisplay.event;

import cc.eumc.eusmapdisplay.model.MapDisplay;
import org.bukkit.entity.Player;

public abstract class DisplayEventHandler {
    public abstract void onCursorPositionChanged(MapDisplay mapDisplay, Player player, int x, int y);

    public abstract void onLeftClick(MapDisplay mapDisplay, Player player, int x, int y);

    public abstract void onRightClick(MapDisplay mapDisplay, Player player, int x, int y);

    public abstract void onWheelScroll(MapDisplay mapDisplay, Player player, int wheelAmt);
}
