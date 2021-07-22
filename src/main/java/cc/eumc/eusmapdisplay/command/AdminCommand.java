package cc.eumc.eusmapdisplay.command;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusmapdisplay.event.DisplayEventHandler;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import cc.eumc.eusmapdisplay.util.TimestampUtil;
import org.bukkit.Bukkit;
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
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabExecutor {
    private static final String HelpString = """
            /mapdisplay list : list existing MapDisplays
            /mapdisplay get : get a copy of MapDisplay
            /mapdisplay test : get test MapDisplay""";

    EusMapDisplay plugin;
    // TODO: reload
    private String[] commands = {"help", "list", "get", "test"};

    public AdminCommand(EusMapDisplay instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("mapdisplay.admin")) {
            if (args.length == 0) {
                sendMessage(sender, "&eUse /mapdisplay help for help");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "get" -> {
                    if (args.length == 1) {
                        sendMessage(sender, "&eUsage: /mapdisplay get <UUID> [PlayerName] [column(x)] [line(y)]");
                        return true;
                    }

                    UUID uuid;
                    Player player;
                    int column = -1; // -1 for all
                    int line = -1;

                    try { // parsing args
                        uuid = UUID.fromString(args[1]);
                        if (args.length > 2) { // with [PlayerName] [..?]
                            player = Bukkit.getPlayer(args[2]);
                            if (player == null) {
                                sendMessage(sender, "&ePlayer %s not found.".formatted(args[2]));
                                return true;
                            }

                            if (args.length > 3) { // with [column] [..?]
                                column = args[3].equals("*") ? -1 : Integer.parseInt(args[3]);

                                if (args.length > 4) { // with [line]
                                    line = args[4].equals("*") ? -1 : Integer.parseInt(args[4]);
                                }
                            }
                        } else if (sender instanceof Player) {
                            player = (Player) sender;
                        } else {
                            sendPlayerOnly(sender);
                            return true;
                        }
                    } catch (Exception ex) {
//                        ex.printStackTrace();
                        sendMessage(sender, String.format("&cError: %s", ex.getMessage()));
                        return true;
                    }

                    MapDisplay mapDisplay = plugin.getMapManager().getMapDisplay(uuid);

                    if (mapDisplay == null) {
                        sendMessage(sender, String.format("MapDisplay %s does not exist.", uuid));
                        return true;
                    }

                    for (int x = (column == -1 ? 0 : column); x <= (column == -1 ? mapDisplay.getWindowWidth() - 1 : column); x++) {
                        for (int y = (line == -1 ? 0 : line); y <= (line == -1 ? mapDisplay.getWindowHeight() - 1 : line); y++) {
                            player.getInventory().addItem(plugin.getMapManager().getMapItem(mapDisplay, x, y));
                        }
                    }
                }

                case "list" -> {
                    sendMessage(sender, "========== MapDisplay List ==========", false);
                    for (MapDisplay mapDisplay : plugin.getMapManager().getMapDisplays()) {
                        sendMessage(sender, "&l%s&r &7|&r Last Access: %s".formatted(mapDisplay.getUniqueId(), TimestampUtil.toFormattedTime(mapDisplay.getLastAccess())), false);
                    }
                }

                case "test" -> {
                    if (sender instanceof Player player) {
                        MapDisplay mapDisplay = plugin.getMapManager().createMap(2, 2, player.getWorld());
                        for (ItemStack item : plugin.getMapManager().getMapItem(mapDisplay)) {
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
                                    mapDisplay.getDisplay().drawRectangle(x0, y0, x, y, MapPalette.DARK_GREEN, MapPalette.LIGHT_GREEN);
                                    mapDisplay.getDisplay().drawLine(x0, y0, x, y, MapPalette.BLUE);
                                }
                                x0 = x;
                                y0 = y;
                            }

                            @Override
                            public void onWheelScroll(MapDisplay mapDisplay, Player player, int wheelAmt) {

                            }
                        });
                    } else {
                        sendPlayerOnly(sender);
                    }
                }

                case "help" -> {
                    sendMessage(sender, HelpString);
                }

                default -> sendMessage(sender, "&eUnknown argument.");
            }
        }
        else {
            sendMessage(sender, "&eSorry.");
        }
        return true;
    }

    private void sendPlayerOnly(CommandSender receiver) {
        sendMessage(receiver, "&ePlayer only command.");
    }

    private void sendMessage(CommandSender receiver, String message) {

        sendMessage(receiver, message, true);
    }

    private void sendMessage(CommandSender receiver, String message, boolean withPrefix) {
        receiver.sendMessage((withPrefix ? "[EusMapDisplay] " : "") + message.replaceAll("&", "§"));
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
