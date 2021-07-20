package cc.eumc.eusmapdisplay.listener;

import cc.eumc.eusmapdisplay.EusMapDisplay;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {
    EusMapDisplay plugin;

    public PlayerListener(EusMapDisplay plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Entity entity = e.getPlayer().getTargetEntity(10, false);
        if (entity == null || entity.getType() != EntityType.ITEM_FRAME) {
            return;
        }

        ItemStack itemStack = ((ItemFrame) entity).getItem();
        if (itemStack.getType() != Material.FILLED_MAP) {
            return;
        }

        List<Component> lores = itemStack.lore();
        if (lores == null || lores.size() != 3) {
            return;
        }

        try {
            // Parse uuid
            String uuidStr = TextComponentUtil.getContent(lores.get(2));
            assert uuidStr != null;
            UUID uuid = UUID.fromString(uuidStr);

            MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(uuid);
            if (mapDisplay == null) {
                return;
            }

            // Parse window coordinate
            String windowCoordinate = TextComponentUtil.getContent(lores.get(1));
            assert windowCoordinate != null;
            String[] split = windowCoordinate.split(", ");
            if (split.length != 2) {
                return;
            }
            int windowX = Integer.parseInt(split[0].substring(1));
            int windowY = Integer.parseInt(split[1].substring(0, split[1].length() - 1));

            Location eyeLocation = new Location(e.getTo().getWorld(), e.getTo().getX(), e.getTo().getY(), e.getTo().getZ());
            eyeLocation.setY(eyeLocation.getY() + 2);
            eyeLocation.setDirection(e.getTo().getDirection());
            Location targetLocation = getTargetPoint(eyeLocation, entity);

            float yaw = eyeLocation.getYaw();
            boolean frameFacingWest = entity.getFacing() == BlockFace.WEST;
            boolean lookingNorthButFrameFacingSouth = Math.abs(yaw) > 135 && entity.getFacing() == BlockFace.SOUTH;
            System.out.println("" + yaw + "; " + entity.getFacing().toString());
            int relativeX = (lookingNorthButFrameFacingSouth || frameFacingWest ? 180 : 0) + (int)((Math.abs(targetLocation.getX()) - Math.abs(entity.getLocation().getX()) + (lookingNorthButFrameFacingSouth || frameFacingWest ? 1 : 0.5)) * 128) * (Math.abs(yaw) > 135 ? -1 : 1);
            int relativeY = 180 - (int)((targetLocation.getY() - entity.getLocation().getY() + 0.5) * 128);

            int[] absoluteCoordinates = Display.getAbsoluteCoordinate(windowX, windowY, relativeX, relativeY);
            System.out.println(Arrays.toString(absoluteCoordinates));

            mapDisplay.getDisplay().setCursorLocation(absoluteCoordinates[0], absoluteCoordinates[1]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

    }


    Location getTargetPoint(Location eyeLocation, Entity entity) {
        Vector vector = eyeLocation.getDirection();
        vector.normalize().multiply(entity.getLocation().distance(eyeLocation));
        eyeLocation.add(vector);
        return eyeLocation;
    }

}
