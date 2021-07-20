package cc.eumc.eusmapdisplay.command;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusmapdisplay.model.Display;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
            if (args[0].equalsIgnoreCase("test")) {
                Player player = (Player) sender;

                MapDisplay mapDisplay = plugin.getMapManager().createMap(2, 2, player.getWorld());
                for(ItemStack item : plugin.getMapManager().getMapItem(mapDisplay)) {
                    player.getInventory().addItem(item);
                }
            }
        }
        else {
            sender.sendMessage("[UniBan] §eSorry.");
        }
        return true;
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
