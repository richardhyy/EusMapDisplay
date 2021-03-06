package cc.eumc.eusmapdisplay.model;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusmapdisplay.event.DefaultCursorMovingHandler;
import cc.eumc.eusmapdisplay.event.DisplayEventHandler;
import cc.eumc.eusmapdisplay.event.DisplayEventType;
import cc.eumc.eusmapdisplay.renderer.DisplayRenderer;
import cc.eumc.eusmapdisplay.util.TimestampUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.*;

public class MapDisplay {
    private final UUID uniqueId;

    private long lastAccess;
    private int[][] viewIds;
    private Display display;
    private transient Map<Integer, MapView> idViewMap;
    private transient World world;
    private transient boolean active = false;

    private transient List<DisplayEventHandler> displayEventHandlerList = null;

//    public MapDisplay(Display display, MapView[][] existingMapViews) {
//        this.display = display;
//        int windowXNumber = display.getWidth() / 128;
//        int windowYNumber = display.getHeight() / 128;
//
//        if (existingMapViews.length != windowXNumber || existingMapViews[0].length != windowYNumber) {
//            throw new IllegalArgumentException(String.format("Existing map array (%dx%d) does not match display dimension (%dx%d)",
//                    existingMapViews.length, existingMapViews[0].length,
//                    windowXNumber, windowYNumber));
//        }
//
//        this.mapViews = existingMapViews;
//
//        setRenderers(windowXNumber, windowYNumber, null);
//    }

    public MapDisplay(UUID uuid, Display display, World world) {
        this.uniqueId = uuid;
        this.display = display;
        this.world = world;

        int windowXNumber = display.getWidth() / 128;
        int windowYNumber = display.getHeight() / 128;

        this.viewIds = new int[windowXNumber][windowYNumber];

        for (int x=0; x < windowXNumber; x++) {
            for (int y=0; y < windowYNumber; y++) {
                viewIds[x][y] = -1;
            }
        }

        displayEventHandlerList = new ArrayList<>();
        displayEventHandlerList.add(new DefaultCursorMovingHandler());
    }

    /**
     * Fire an MapDisplay event
     * @param type event type
     * @param player player who triggered the event
     * @param value1 the first value of the event, leave null if not required
     * @param value2 the second value of the event, leave null if not required
     */
    public void triggerEvent(DisplayEventType type, Player player, Integer value1, Integer value2) {
//        System.out.printf("[%s] %d %d%n", type.toString(), value1, value1);

        // Add cursor moving handler if displayEventHandlerList is null
        if (displayEventHandlerList == null) {
            displayEventHandlerList = new ArrayList<>();
            displayEventHandlerList.add(new DefaultCursorMovingHandler());
        }

        for (DisplayEventHandler handler : displayEventHandlerList.toArray(new DisplayEventHandler[0])) {
            try {
                switch (type) {
                    case CURSOR_MOVE -> handler.onCursorPositionChanged(this, player, value1, value2);
                    case LEFT_CLICK -> handler.onLeftClick(this, player, value1, value2);
                    case RIGHT_CLICK -> handler.onRightClick(this, player, value1, value2);
                    case WHEEL_SCROLL -> handler.onWheelScroll(this, player, value1);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Bind DisplayEventHandler to the MapDisplay
     * @param handler event handler to register
     */
    public void registerEventHandler(DisplayEventHandler handler) {
        if (displayEventHandlerList == null) {
            displayEventHandlerList = new ArrayList<>();
        }

        displayEventHandlerList.add(handler);
    }

    /**
     * Remove an existing DisplayEventHandler
     * @param handler event handler to remove
     * @return true if succeed
     */
    public boolean removeEventHandler(DisplayEventHandler handler) {
        if (displayEventHandlerList == null) {
            displayEventHandlerList = new ArrayList<>();
            return false;
        }

        return displayEventHandlerList.remove(handler);
    }

    /**
     * Get all the DisplayEventHandlers
     * @return an array of DisplayEventHandlers
     */
    public DisplayEventHandler[] getEventHandlers() {
        if (displayEventHandlerList == null) {
            displayEventHandlerList = new ArrayList<>();
        }

        return displayEventHandlerList.toArray(new DisplayEventHandler[0]);
    }

//    private void setRenderer(int windowXNumber, int windowYNumber, World world) {
//        for (int x=0; x<windowXNumber; x++) {
//            for (int y=0; y<windowYNumber; y++) {
//                MapView _map = mapViews[x][y] == null ? Bukkit.createMap(world): mapViews[x][y];
//
//                DisplayRenderer _renderer = new DisplayRenderer(display, x, y);
//                _map.getRenderers().forEach(_map::removeRenderer);
//                _map.addRenderer(_renderer);
//
//                this.mapViews[x][y] = _map;
//            }
//        }
//    }

    private MapView initMap(int windowX, int windowY, MapView mapView) {
        MapView map = mapView == null ? Bukkit.createMap(world) : mapView;
        DisplayRenderer renderer = new DisplayRenderer(display, windowX, windowY);
        map.getRenderers().forEach(map::removeRenderer);
        map.addRenderer(renderer);
        this.viewIds[windowX][windowY] = map.getId();


        if (idViewMap == null) {
            idViewMap = new HashMap<>();
        }
        this.idViewMap.put(map.getId(), map);

        // set active if any part of the display is bound with a MapView
        active = true;

        EusMapDisplay.getInstance().sendInfo(String.format("MapView %d <~> %s", map.getId(), uniqueId));

        return map;
    }

    /**
     * Create MapViews bound with MapDisplay. Should be called right after object constructed.
     */
    public void initMaps() {
        for (int x = 0; x < viewIds.length; x++) {
            for (int y = 0; y < viewIds[0].length; y++) {
                assignMapView(x, y, null);
            }
        }
    }

    /**
     * Create new map or bind with existing map for displaying
     * This will updating lastAccess by the current timestamp
     * @param windowX x coordinate for the MapView within the entire Display
     * @param windowY y coordinate for the MapView within the entire Display
     * @param mapView leave null for creating a new map
     */
    public void assignMapView(int windowX, int windowY, MapView mapView) {
        // A MapInitializeEvent will trigger updating `lastAccess`
        this.lastAccess = TimestampUtil.getSecondsSince1970();
        
        initMap(windowX, windowY, mapView);
    }

    /**
     * Get Display of the MapDisplay
     * @return Display
     */
    public Display getDisplay() {
        return display;
    }

    /**
     * Get ids of all the MapViews
     * @return array of MapView ID for each part
     */
    public int[][] getViewIds() {
        return viewIds;
    }

    /**
     * Get all MapViews bound with the MapDisplay
     * New MapView will be created if any part of the MapDisplay has not yet been initialized
     * @return array of MapView for each part
     */
    public MapView[][] getMapViews() {
        MapView[][] mapViews = new MapView[viewIds.length][viewIds[0].length];
        if (idViewMap != null) {
            for (int x = 0; x < viewIds.length; x++) {
                for (int y = 0; y < viewIds[0].length; y++) {
                    int _id = viewIds[x][y];
                    MapView _mapView = _id > -1 ? idViewMap.get(_id) : initMap(x, y, null);
                    mapViews[x][y] = _mapView;
                }
            }
        }
        return mapViews;
    }

    /**
     * Force all MapViews to render immediately.
     */
    public void forceRender() {
        MapView[][] mapViews = getMapViews();
        for (MapView[] mapViewColumn : mapViews) {
            for (int y = 0; y < mapViews[0].length; y++) {
                // mapViewColumn[y] = null if forceRender() is called before MapView being initialized
                if (mapViewColumn[y] == null) {
                    return;
                }
                for (MapRenderer renderer : mapViewColumn[y].getRenderers()) {
                    if (renderer instanceof DisplayRenderer displayRenderer) {
                        displayRenderer.render();
                    }
                }
            }
        }
    }

    /**
     * Pixel width divided by 128
     * @return window width
     */
    public int getWindowWidth() {
        return getDisplay().getWidth() / 128;
    }

    /**
     * Pixel height divided by 128
     * @return window height
     */
    public int getWindowHeight() {
        return getDisplay().getHeight() / 128;
    }

    /**
     * Get the unique id of the MapDisplay
     * @return UUID of the MapDisplay
     */
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Get last access timestamp
     * Be updated when any part of the MapDisplay being loaded
     * Generated by TimestampUtil
     * @return last time the MapDisplay get accessed
     */
    public long getLastAccess() {
        return lastAccess;
    }

    /**
     * Check if the MapDisplay is active, i.e. any part of it has been bound with MapView
     * @return active if true
     */
    public boolean isActive() {
        return active;
    }
}
