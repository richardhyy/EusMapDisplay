package cc.eumc.eusmapdisplay.listener;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {
    EusMapDisplay plugin;

    public PlayerListener(EusMapDisplay plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

    }
}
