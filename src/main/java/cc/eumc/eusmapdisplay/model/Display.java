package cc.eumc.eusmapdisplay.model;

import org.bukkit.map.MapPalette;

import java.awt.*;
import java.awt.image.BufferedImage;

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

    /**
     * Get width (unit: pixel)
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get height (unit: pixel)
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get x coordinate for the cursor
     * @return
     */
    public int getCursorX() {
        return cursorX;
    }

    /**
     * Get y coordinate for the cursor
     * @return
     */
    public int getCursorY() {
        return cursorY;
    }

    /**
     * Set cursor coordinates
     * @param x
     * @param y
     */
    public void setCursorLocation(int x, int y) {
        this.cursorX = x;
        this.cursorY = y;
    }

    /**
     * Get all pixels of the Display
     * @param withCursor
     * @return
     */
    public byte[][] getPixels(boolean withCursor) {
        return getPixels(withCursor, 0, 0, width - 1, height - 1);
    }

    /**
     * Get part of the pixels of the Display
     * @param withCursor draw cursor
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return
     */
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

    /**
     * Draw pixel on the display
     * @param x
     * @param y
     * @param value MapView color
     */
    public void setPixel(int x, int y, byte value) {
        this.pixels[x][y] = value;
    }

    /**
     * Draw line segment on the display
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param color MapView color
     */
    public void drawLine(int x0, int y0, int x1, int y1, byte color) {
        int dx = Math.abs(x1-x0);
        int sx = x0<x1 ? 1 : -1;
        int dy = -Math.abs(y1-y0);
        int sy = y0<y1 ? 1 : -1;
        int err = dx+dy;  /* error value e_xy */
        while (true) {  /* loop */
            setPixel(x0, y0, color);
            if (x0 == x1 && y0 == y1) {
                break;
            }

            int e2 = 2 * err;
            if (e2 >= dy) { /* e_xy+e_x > 0 */
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) { /* e_xy+e_y < 0 */
                err += dx;
                y0 += sy;
            }
        }
    }

    /**
     * Draw rectangle on the Display
     * Outline is 1px in width
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param fillColor MapView color
     * @param outlineColor MapView color
     */
    public void drawRectangle(int x0, int y0, int x1, int y1, byte fillColor, byte outlineColor) {
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
            drawLine(x0, y0, x1, y0, outlineColor);
            drawLine(x0, y1, x1, y1, outlineColor);
            drawLine(x0, y0, x0, y1, outlineColor);
            drawLine(x1, y0, x1, y1, outlineColor);
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
     * Draw text on the Display
     * @param x
     * @param y
     * @param text
     * @param font awt font
     * @param color awt color
     */
    public void drawText(int x, int y, String text, Font font, Color color) {
        Color transparentColor = new Color(color.getRed() == 255 ? 254 : 255, color.getGreen() == 255 ? 254 : 255, color.getBlue() == 255 ? 254 : 255);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setFont(font);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.setColor(transparentColor);
        graphics2D.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics2D.setColor(color);
        graphics2D.drawString(text, 0, fontMetrics.getAscent());
        graphics2D.dispose();
        drawImage(x, y, image, transparentColor);
    }

    /**
     * Draw image on the Display
     * @param x
     * @param y
     * @param image
     * @param transparentColor awt color
     */
    public void drawImage(int x, int y, BufferedImage image, Color transparentColor) {
        for (int _x = 0; _x < image.getWidth(); _x++) {
            for (int _y = 0; _y < image.getHeight(); _y++) {
                int absoluteX = _x + x;
                int absoluteY = _y + y;
                if (absoluteX < 0 || absoluteX > width - 1 || absoluteY < 0 || absoluteY> height - 1) {
                    continue;
                }

                Color pixelColor = new Color(image.getRGB(_x, _y));
                if (!pixelColor.equals(transparentColor)) {
                    pixels[absoluteX][absoluteY] = MapPalette.matchColor(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
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
