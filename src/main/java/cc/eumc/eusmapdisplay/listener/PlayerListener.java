package cc.eumc.eusmapdisplay.listener;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusmapdisplay.event.DisplayEventType;
import cc.eumc.eusmapdisplay.model.Display;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import cc.eumc.eusmapdisplay.util.DirectionUtil;
import cc.eumc.eusmapdisplay.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerListener implements Listener {
    static Material[] IndicatorTypes = new Material[] {Material.BLACK_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.CYAN_WOOL, Material.GRAY_WOOL, Material.GREEN_WOOL, Material.LIGHT_BLUE_WOOL, Material.LIGHT_GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.LIME_WOOL, Material.MAGENTA_WOOL, Material.ORANGE_WOOL, Material.PINK_WOOL};
    EusMapDisplay plugin;

    public PlayerListener(EusMapDisplay plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }

        TargetDisplay[] targetDisplays = getTargetMapDisplay(e.getPlayer(), e.getPlayer().getLocation());
        if (targetDisplays == null) {
            return;
        }

        for (TargetDisplay targetDisplay : targetDisplays) {
            MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(targetDisplay.uuid);
            mapDisplay.triggerEvent(DisplayEventType.CURSOR_MOVE, e.getPlayer(), targetDisplay.absoluteX, targetDisplay.absoluteY);
        }

        // DO NOT CANCEL EVENT HERE
    }

    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        TargetDisplay[] targetDisplays = getTargetMapDisplay(e.getPlayer(), e.getPlayer().getLocation());
        if (targetDisplays == null) {
            return;
        }

        for (TargetDisplay targetDisplay : targetDisplays) {
            MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(targetDisplay.uuid);
            mapDisplay.triggerEvent(DisplayEventType.LEFT_CLICK, e.getPlayer(), targetDisplay.absoluteX, targetDisplay.absoluteY);
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent e) {
        TargetDisplay[] targetDisplays = getTargetMapDisplay(e.getPlayer(), e.getPlayer().getLocation());
        if (targetDisplays == null) {
            return;
        }

        for (TargetDisplay targetDisplay : targetDisplays) {
            MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(targetDisplay.uuid);
            mapDisplay.triggerEvent(DisplayEventType.LEFT_CLICK, e.getPlayer(), targetDisplay.absoluteX, targetDisplay.absoluteY);
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBreakHang(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player player)) {
            return;
        }

        TargetDisplay[] targetDisplays = getTargetMapDisplay(player, player.getLocation());
        if (targetDisplays == null) {
            return;
        }

        for (TargetDisplay targetDisplay : targetDisplays) {
            MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(targetDisplay.uuid);
            mapDisplay.triggerEvent(DisplayEventType.LEFT_CLICK, player, targetDisplay.absoluteX, targetDisplay.absoluteY);
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEntityEvent e) {
        TargetDisplay[] targetDisplays = getTargetMapDisplay(e.getPlayer(), e.getPlayer().getLocation());
        if (targetDisplays == null) {
            return;
        }

        for (TargetDisplay targetDisplay : targetDisplays) {
            MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(targetDisplay.uuid);
            mapDisplay.triggerEvent(DisplayEventType.RIGHT_CLICK, e.getPlayer(), targetDisplay.absoluteX, targetDisplay.absoluteY);
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerScrollWheel(PlayerItemHeldEvent e) {
        if (e.isCancelled()) {
            return;
        }

        TargetDisplay[] targetDisplays = getTargetMapDisplay(e.getPlayer(), e.getPlayer().getLocation());
        if (targetDisplays == null) {
            return;
        }

        for (TargetDisplay targetDisplay : targetDisplays) {
            MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(targetDisplay.uuid);
            int delta = e.getNewSlot() - e.getPreviousSlot();
            mapDisplay.triggerEvent(DisplayEventType.WHEEL_SCROLL, e.getPlayer(), Math.abs(delta) >= 8 ? (Math.abs(delta) - 7) * (delta > 0 ? 1 : -1) : delta, null);
        }
    }

    /**
     * Get MapDisplays attached to the target block
     * @param player
     * @param playerStandAt
     * @return null if no MapDisplay attached to target block
     */
    @Nullable TargetDisplay[] getTargetMapDisplay(Player player, Location playerStandAt) {
        Block targetBlock = player.getTargetBlock(10);
        if (targetBlock == null || !targetBlock.isSolid()) {
            return null;
        }

        // For debugging
        targetBlock.setType(IndicatorTypes[new Random().nextInt(IndicatorTypes.length)]);

        List<TargetDisplay> mapDisplays = new ArrayList<>();
        List<ItemFrame> itemFrames = targetBlock.getLocation().getNearbyEntitiesByType(ItemFrame.class, 2, 1, 2).stream()
                .filter(itemFrame -> (itemFrame.getLocation().getBlockX() == targetBlock.getX() || itemFrame.getLocation().getBlockZ() == targetBlock.getZ())
                        && itemFrame.getFacing() == DirectionUtil.getOpposite(DirectionUtil.standardize(player.getFacing()))) // must be directly attached to the block
                .collect(Collectors.toList());
        for (ItemFrame entity : itemFrames) {
            ItemStack itemStack = entity.getItem();
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

                TargetDisplay targetDisplay = new TargetDisplay();
                targetDisplay.uuid = uuid;
                targetDisplay.windowX = Integer.parseInt(split[0].substring(1));
                targetDisplay.windowY = Integer.parseInt(split[1].substring(0, split[1].length() - 1));
                int[] absoluteCoordinates = getTargetAbsoluteCoordinates(targetDisplay.windowX, targetDisplay.windowY, entity, playerStandAt);
                targetDisplay.absoluteX = absoluteCoordinates[0];
                targetDisplay.absoluteY = absoluteCoordinates[1];
                mapDisplays.add(targetDisplay);

                return mapDisplays.toArray(new TargetDisplay[0]);
            } catch (Exception ignore) { }
        }
        return null;
    }

    int[] getTargetAbsoluteCoordinates(int windowX, int windowY, Entity frame, Location playerLocation) {
        Location eyeLocation = new Location(playerLocation.getWorld(), playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
        eyeLocation.setY(eyeLocation.getY() + 2);
        eyeLocation.setDirection(playerLocation.getDirection());

        Location targetLocation = getTargetPoint(eyeLocation, frame);
        Location frameLocation = frame.getLocation();

        boolean facingXAxis = frame.getFacing() == BlockFace.EAST || frame.getFacing() == BlockFace.WEST;
        double targetCenterX = facingXAxis ? targetLocation.getZ() : targetLocation.getX();

        double frameCenterX = facingXAxis ? frameLocation.getZ() : frameLocation.getX();

        boolean flip = frame.getFacing() == BlockFace.EAST || frame.getFacing() == BlockFace.NORTH;

        int relativeX = (int) ((targetCenterX - frameCenterX + (frame.getFacing() == BlockFace.EAST || frame.getFacing() == BlockFace.NORTH ? -0.5 : 0.5)) * 128) * (flip ? -1 : 1);

        int relativeY = 180 - (int)((targetLocation.getY() - frame.getLocation().getY() + 0.5) * 128);

        return Display.getAbsoluteCoordinate(windowX, windowY, relativeX, relativeY);
    }

    Location getTargetPoint(Location eyeLocation, Entity entity) {
        Vector vector = eyeLocation.getDirection();
        vector.normalize().multiply(entity.getLocation().distance(eyeLocation));
        eyeLocation.add(vector);
        return eyeLocation;
    }


    class TargetDisplay {
        public UUID uuid;
        public int windowX;
        public int windowY;
        public int absoluteX;
        public int absoluteY;
    }
}
