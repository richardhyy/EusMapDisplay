package cc.eumc.eusmapdisplay.manager;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusmapdisplay.model.Display;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import cc.eumc.eusmapdisplay.storage.DisplayStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalMapManager {
    EusMapDisplay plugin;
    DisplayStorage displayStorage;

    public GlobalMapManager(EusMapDisplay plugin, DisplayStorage displayStorage) {
        this.plugin = plugin;
        this.displayStorage = displayStorage;
    }

    /**
     * Create a new MapDisplay
     * @param windowWidth number of MapView for the MapDisplay's width
     * @param windowHeight number of MapView for the MapDisplay's height
     * @param world the world the MapView belongs to
     * @return MapDisplay
     */
    public MapDisplay createMap(int windowWidth, int windowHeight, World world) {
        Display display = new Display(windowWidth * 128, windowHeight * 128);
        MapDisplay mapDisplay = new MapDisplay(UUID.randomUUID(), display, world);
        displayStorage.addMapDisplay(mapDisplay);
        mapDisplay.initMaps();
        return mapDisplay;
    }

    /**
     * Get an array of FILLED_MAP ItemStack bound with the MapDisplay
     * Content of lores on the second and third line are used for identifying and SHOULD NOT BE MODIFIED
     * @param mapDisplay existing MapDisplay
     * @return
     */
    public ItemStack[] getMapItem(MapDisplay mapDisplay) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (int y = 0; y < mapDisplay.getMapViews()[0].length; y++) {
            for (int x = 0; x < mapDisplay.getMapViews().length; x++) {
                ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = ((MapMeta) itemStack.getItemMeta());
                meta.setMapView(mapDisplay.getMapViews()[x][y]);
                List<Component> loreList = new ArrayList<>();
                loreList.add(Component.text("Map Display"));
                loreList.add(Component.text(String.format("(%d, %d)", x, y)));
                loreList.add(Component.text(mapDisplay.getUniqueId().toString()).style(Style.style(TextColor.color(60, 60, 110))));
                meta.lore(loreList);
                itemStack.setItemMeta(meta);
                itemStacks.add(itemStack);
            }
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    /**
     * Get all MapDisplays
     * @return
     */
    public MapDisplay[] getMapDisplays() {
        return displayStorage.getMapDisplays();
    }

    /**
     * Get MapDisplay by UUID
     * @param uuid
     * @return
     */
    public MapDisplay getMapDisplay(UUID uuid) {
        return displayStorage.getMapDisplay(uuid);
    }

    /**
     * Get MapDisplay which contains MapView that matches the given id
     * @param id MapView id
     * @param refWindowX
     * @param refWindowY
     * @return MapDisplay or null if not exist
     */
    public MapDisplay getMapDisplayFromMapViewId(int id, AtomicReference<Integer> refWindowX, AtomicReference<Integer> refWindowY) {
        return displayStorage.matchMapDisplay(mapDisplay -> {
            int[][] viewIds = mapDisplay.getViewIds();
//            MapView[][] mapViews = mapDisplay.getMapViews();
            for (int x = 0; x < viewIds.length; x++) {
                for (int y = 0; y < viewIds[0].length; y++) {
                    if (viewIds[x][y] != -1 && viewIds[x][y] == id) {
                        refWindowX.set(x);
                        refWindowY.set(y);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /**
     * Save all MapDisplay to disk
     */
    public void save() {
        displayStorage.save();
    }
}
