package cc.eumc.eusmapdisplay.event;

public abstract class DisplayHandler {
    abstract void onCursorPositionChanged(int x, int y);

    abstract void onLeftClick(int x, int y);

    abstract void onRightClick(int x, int y);

    abstract void onWheelScroll(int wheelAmt);
}
