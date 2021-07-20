package cc.eumc.eusmapdisplay.model;

import org.bukkit.map.MapPalette;

public class Display {
    private final int width;
    private final int height;
    private int cursorX;
    private int cursorY;
    private final DisplayCursor cursor;

    private final byte[][] pixels;

    public Display(int width, int height) {
        this(width, height, 8, 8, new byte[width][height]);
    }

    public Display(int width, int height, int cursorX, int cursorY, byte[][] pixels) {
        this.width = width;
        this.height = height;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.pixels = pixels;
        this.cursor = new DisplayCursor(MapPalette.DARK_GRAY, MapPalette.WHITE);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public void setCursorLocation(int x, int y) {
        this.cursorX = x;
        this.cursorY = y;
    }

    public byte[][] getPixels(boolean withCursor) {
        return getPixels(withCursor, 0, 0, width - 1, height - 1);
    }

    public byte[][] getPixels(boolean withCursor, int startX, int startY, int endX, int endY) {
        byte[][] canvas = new byte[pixels.length][pixels[0].length];
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                canvas[x][y] = pixels[x][y];
            }
        }

        if (withCursor) {
            byte[][] cursorPixels = cursor.getPixels();
            for (int x = 0; x < cursorPixels.length; x++) {
                for (int y = 0; y < cursorPixels[0].length; y++) {
                    // ignore pixel if out of the window
                    if (cursorX + cursor.getOffsetX() + x < startX || cursorX + cursor.getOffsetX() + x> endX ||
                        cursorY + cursor.getOffsetY() + y < startY || cursorY + cursor.getOffsetY() + y > endY) {
                        continue;
                    }

                    byte pixel = cursorPixels[x][y];
                    if (pixel != 0) {
                        canvas[cursorX + x + cursor.getOffsetX()][cursorY + y + cursor.getOffsetY()] = pixel;
                    }
                }
            }
        }

        byte[][] result = new byte[endX - startX + 1][endY - startY + 1];
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                result[x - startX][y - startY] = canvas[x][y];
            }
        }
        return result;
    }

    public void setPixel(int x, int y, byte value) {
        this.pixels[x][y] = value;
    }

    public void plotLine(int x0, int y0, int x1, int y1, byte color) {
        int t;
        if (x1 < x0) {
            t = x1;
            x1 = x0;
            x0 = t;
        }
        if (y1 < y0) {
            t = y1;
            y1 = y0;
            y0 = t;
        }

        int dx = x1 - x0;
        int dy = y1 - y0;
        int D = 2*dy - dx;
        int y = y0;

        for (int x = x0; x <= x1; x++) {
            setPixel(x, y, color);
            if (D > 0) {
                y = y + 1;
                D = D - 2 * dx;
            }
            D = D + 2 * dy;
        }
    }

    public void plotRectangle(int x0, int y0, int x1, int y1, byte fillColor, byte outlineColor) {
        int t;
        if (x1 < x0) {
            t = x1;
            x1 = x0;
            x0 = t;
        }
        if (y1 < y0) {
            t = y1;
            y1 = y0;
            y0 = t;
        }

        if (outlineColor != 0) {
            plotLine(x0, y0, x1, y0, outlineColor);
            plotLine(x0, y1, x1, y1, outlineColor);
            plotLine(x0, y0, x0, y1, outlineColor);
            plotLine(x1, y0, x1, y1, outlineColor);
        }

        if (fillColor != 0) {
            for (int x = x0 + 1; x < x1 - 1; x++) {
                for (int y = y0 + 1; y < y1 - 1; y++) {
                    setPixel(x, y, fillColor);
                }
            }
        }
    }

    /**
     * Get global coordinates in display from coordinates in sub-window.
     * @param windowX
     * @param windowY
     * @param inWindowX Range: 0-127
     * @param inWindowY Range: 0-127
     * @return
     */
    public static int[] getAbsoluteCoordinate(int windowX, int windowY, int inWindowX, int inWindowY) {
        return new int[] {
                windowX * 128 + inWindowX,
                windowY * 128 + inWindowY
        };
    }
}
