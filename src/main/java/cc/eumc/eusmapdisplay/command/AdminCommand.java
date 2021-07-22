package cc.eumc.eusmapdisplay.command;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusmapdisplay.event.DisplayEventHandler;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapPalette;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabExecutor {
    EusMapDisplay plugin;
    private String[] commands = {"help", "test", "reload"};

    public AdminCommand(EusMapDisplay instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("mapdisplay.admin")) {
            if (args[0].equalsIgnoreCase("test") && sender instanceof Player player) {
                MapDisplay mapDisplay = plugin.getMapManager().createMap(2, 2, player.getWorld());
                for(ItemStack item : plugin.getMapManager().getMapItem(mapDisplay)) {
                    player.getInventory().addItem(item);
                }
                mapDisplay.registerEventHandler(new DisplayEventHandler() {
                    int x0 = -1;
                    int y0 = -1;

                    @Override
                    public void onCursorPositionChanged(MapDisplay mapDisplay, Player player, int x, int y) {

                    }

                    @Override
                    public void onLeftClick(MapDisplay mapDisplay, Player player, int x, int y) {
                        mapDisplay.getDisplay().drawText(x, y, String.format("(%d, %d)", x, y), new Font("Arial", Font.BOLD, 14), Color.CYAN);
                    }

                    @Override
                    public void onRightClick(MapDisplay mapDisplay, Player player, int x, int y) {
                        if (x0 > -1 && y0 > -1) {
                            mapDisplay.getDisplay().plotRectangle(x0, y0, x, y, MapPalette.DARK_GREEN, MapPalette.LIGHT_GREEN);
                            mapDisplay.getDisplay().plotLine(x0, y0, x, y, MapPalette.BLUE);
                        }
                        x0 = x;
                        y0 = y;
                    }

                    @Override
                    public void onWheelScroll(MapDisplay mapDisplay, Player player, int wheelAmt) {

                    }
                });
            } else {
                sendMessage(sender, "&ePlayer only command.");
            }
        }
        else {
            sendMessage(sender, "&eSorry.");
        }
        return true;
    }

    private void sendMessage(CommandSender receiver, String message) {
        receiver.sendMessage("[EusMapDisplay] " + message.replaceAll("&", "§"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("mapdisplay.admin")) return new ArrayList<>();

        if (args.length > 1)
            return new ArrayList<>();
        else if (args.length == 1)
            return Arrays.stream(commands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        else
            return Arrays.asList(commands);
    }
}
