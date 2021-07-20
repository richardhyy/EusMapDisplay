package cc.eumc.eusmapdisplay.listener;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusmapdisplay.event.DisplayEventType;
import cc.eumc.eusmapdisplay.model.Display;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import cc.eumc.eusmapdisplay.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerListener implements Listener {
    EusMapDisplay plugin;

    public PlayerListener(EusMapDisplay plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Entity entity = e.getPlayer().getTargetEntity(10, false);

        AtomicReference<Integer> windowX = new AtomicReference<>();
        AtomicReference<Integer> windowY = new AtomicReference<>();

        UUID uuid = getTargetMapDisplay(entity, windowX, windowY);
        if (uuid == null) {
            return;
        }

        int[] absoluteCoordinates = getTargetAbsoluteCoordinates(windowX.get(), windowY.get(), entity, e.getTo());

        MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(uuid);

        mapDisplay.getDisplay().setCursorLocation(absoluteCoordinates[0], absoluteCoordinates[1]);

        mapDisplay.triggerEvent(DisplayEventType.CURSOR_MOVE, e.getPlayer(), absoluteCoordinates[0], absoluteCoordinates[1]);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK ||
              e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Entity entity = e.getPlayer().getTargetEntity(10, false);

        AtomicReference<Integer> windowX = new AtomicReference<>();
        AtomicReference<Integer> windowY = new AtomicReference<>();

        UUID uuid = getTargetMapDisplay(entity, windowX, windowY);
        if (uuid == null) {
            return;
        }

        int[] absoluteCoordinates = getTargetAbsoluteCoordinates(windowX.get(), windowY.get(), entity, e.getPlayer().getLocation());

        MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(uuid);

        switch (e.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK ->
                    mapDisplay.triggerEvent(DisplayEventType.LEFT_CLICK, e.getPlayer(), absoluteCoordinates[0], absoluteCoordinates[1]);
            default ->
                    mapDisplay.triggerEvent(DisplayEventType.RIGHT_CLICK, e.getPlayer(), absoluteCoordinates[0], absoluteCoordinates[1]);
        }
    }

    @EventHandler
    public void onPlayerScrollWheel(PlayerItemHeldEvent e) {
        Entity entity = e.getPlayer().getTargetEntity(10, false);

        AtomicReference<Integer> windowX = new AtomicReference<>();
        AtomicReference<Integer> windowY = new AtomicReference<>();

        UUID uuid = getTargetMapDisplay(entity, windowX, windowY);
        if (uuid == null) {
            return;
        }

        MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(uuid);
        mapDisplay.triggerEvent(DisplayEventType.WHEEL_SCROLL,e.getPlayer(), e.getNewSlot() - e.getPreviousSlot(), null);
    }

    UUID getTargetMapDisplay(Entity entity, AtomicReference<Integer> refWindowX, AtomicReference<Integer> refWindowY) {
        if (entity == null || entity.getType() != EntityType.ITEM_FRAME) {
            return null;
        }

        ItemStack itemStack = ((ItemFrame) entity).getItem();
        if (itemStack.getType() != Material.FILLED_MAP) {
            return null;
        }

        List<Component> lores = itemStack.lore();
        if (lores == null || lores.size() != 3) {
            return null;
        }

        try {
            // Parse uuid
            String uuidStr = TextComponentUtil.getContent(lores.get(2));
            assert uuidStr != null;
            UUID uuid = UUID.fromString(uuidStr);

            MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(uuid);
            if (mapDisplay == null) {
                return null;
            }

            // Parse window coordinate
            String windowCoordinate = TextComponentUtil.getContent(lores.get(1));
            assert windowCoordinate != null;
            String[] split = windowCoordinate.split(", ");
            if (split.length != 2) {
                return null;
            }

            refWindowX.set(Integer.parseInt(split[0].substring(1)));
            refWindowY.set(Integer.parseInt(split[1].substring(0, split[1].length() - 1)));

            return uuid;
        } catch (Exception ignore) { }

        return null;
    }

    int[] getTargetAbsoluteCoordinates(int windowX, int windowY, Entity frame, Location playerLocation) {
        Location eyeLocation = new Location(playerLocation.getWorld(), playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
        eyeLocation.setY(eyeLocation.getY() + 2);
        eyeLocation.setDirection(playerLocation.getDirection());
        Location targetLocation = getTargetPoint(eyeLocation, frame);

        float yaw = eyeLocation.getYaw();
        boolean frameFacingWest = frame.getFacing() == BlockFace.WEST;
        boolean lookingNorthButFrameFacingSouth = Math.abs(yaw) > 135 && frame.getFacing() == BlockFace.SOUTH;
        int relativeX = (lookingNorthButFrameFacingSouth || frameFacingWest ? 180 : 0) + (int)((Math.abs(targetLocation.getX()) - Math.abs(frame.getLocation().getX()) + (lookingNorthButFrameFacingSouth || frameFacingWest ? 1 : 0.5)) * 128) * (Math.abs(yaw) > 135 ? -1 : 1);
        int relativeY = 180 - (int)((targetLocation.getY() - frame.getLocation().getY() + 0.5) * 128);

        return Display.getAbsoluteCoordinate(windowX, windowY, relativeX, relativeY);
    }

    Location getTargetPoint(Location eyeLocation, Entity entity) {
        Vector vector = eyeLocation.getDirection();
        vector.normalize().multiply(entity.getLocation().distance(eyeLocation));
        eyeLocation.add(vector);
        return eyeLocation;
    }

}
