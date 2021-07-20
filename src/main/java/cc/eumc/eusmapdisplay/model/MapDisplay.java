package cc.eumc.eusmapdisplay.model;

import cc.eumc.eusmapdisplay.renderer.DisplayRenderer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.map.MapView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapDisplay {
    private final UUID uniqueId;

    private int[][] viewIds;
    private Display display;
    private transient Map<Integer, MapView> idViewMap;
    private transient World world;

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

    /**
     * Create new map for displaying
     * @param mapView leave null for creating new map
     * @param windowX
     * @param windowY
     * @return mapId
     */
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

        System.out.printf("MapView %d <~> %s%n", map.getId(), uniqueId);

        return map;
    }

    /**
     * Create MapViews bound with MapDisplay. Should be called right after object constructed.
     */
    public void initMaps() {
        for (int x = 0; x < viewIds.length; x++) {
            for (int y = 0; y < viewIds[0].length; y++) {
                setMapView(x, y, null);
            }
        }
    }

    public void setMapView(int windowX, int windowY, MapView mapView) {
        initMap(windowX, windowY, mapView);
    }

    public Display getDisplay() {
        return display;
    }

    public int[][] getViewIds() {
        return viewIds;
    }

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

    public UUID getUniqueId() {
        return uniqueId;
    }
}
