package cc.eumc.eusmapdisplay.listener;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;

import java.util.concurrent.atomic.AtomicReference;

public class MapListener implements Listener {
    EusMapDisplay plugin;

    public MapListener(EusMapDisplay plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMapInitialize(MapInitializeEvent e) {
        AtomicReference<Integer> windowX = new AtomicReference<>();
        AtomicReference<Integer> windowY = new AtomicReference<>();
        MapDisplay mapDisplay = plugin.getMapManager().getMapDisplayFromMapViewId(e.getMap().getId(), windowX, windowY);
        // Check if the target is a part of MapDisplay
        if (mapDisplay != null) {
            mapDisplay.assignMapView(windowX.get(), windowY.get(), e.getMap());
        }
    }
}
