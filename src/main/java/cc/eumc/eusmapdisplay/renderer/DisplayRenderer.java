package cc.eumc.eusmapdisplay.renderer;

import cc.eumc.eusmapdisplay.model.Display;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisplayRenderer extends MapRenderer {
    private final Display display;
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;

    private MapView map;
    private MapCanvas canvas;
    private Player player;

    public DisplayRenderer(Display display, int windowX, int windowY) {
        super(true);
        this.display = display;

        this.startX = windowX * 128;
        this.startY = windowY * 128;
        this.endX = startX + 127;
        this.endY = startY + 127;

        // validation
        if (startX < 0 || display.getWidth() < endX + 1) {
            throw new IllegalArgumentException("Renderer window length out of range.");
        } else if (startY < 0 || display.getHeight() < endY + 1) {
            throw new IllegalArgumentException("Renderer window height out of range.");
        }

//        System.out.printf("Start: x %d  | y %d%n", startX, startY);
//        System.out.printf("End  : x %d  | y %d%n", endX, endY);
    }

    @Override
    public void render(@Nullable MapView map, @Nullable MapCanvas canvas, @Nullable Player player) {
        if (canvas == null) {
            return;
        }

        if (this.map != map) {
            this.map = map;
        }
        if (this.canvas != canvas) {
            this.canvas = canvas;
        }
        if (this.player != player) {
            this.player = player;
        }


        byte[][] pixels = display.getPixels(true, startX, startY, endX, endY);
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int _x = x - startX;
                int _y = y - startY;
                canvas.setPixel(_x, _y, pixels[_x][_y]);
            }
        }
    }

    /**
     * Force render
     */
    public void render() {
        render(map, canvas, player);
    }
}
