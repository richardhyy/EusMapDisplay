package cc.eumc.eusmapdisplay.event;

import cc.eumc.eusmapdisplay.model.MapDisplay;
import org.bukkit.entity.Player;

public class DefaultCursorMovingHandler extends DisplayEventHandler {
    @Override
    public void onCursorPositionChanged(MapDisplay mapDisplay, Player player, int x, int y) {
        mapDisplay.getDisplay().setCursorLocation(x, y);
    }

    @Override
    public void onLeftClick(MapDisplay mapDisplay, Player player, int x, int y) {
        mapDisplay.getDisplay().setCursorLocation(x, y);
    }

    @Override
    public void onRightClick(MapDisplay mapDisplay, Player player, int x, int y) {
        mapDisplay.getDisplay().setCursorLocation(x, y);
    }

    @Override
    public void onWheelScroll(MapDisplay mapDisplay, Player player, int wheelAmt) {

    }
}
