package cc.eumc.eusmapdisplay;

import cc.eumc.eusmapdisplay.command.AdminCommand;
import cc.eumc.eusmapdisplay.listener.MapListener;
import cc.eumc.eusmapdisplay.listener.PlayerListener;
import cc.eumc.eusmapdisplay.manager.GlobalMapManager;
import cc.eumc.eusmapdisplay.storage.DisplayStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class EusMapDisplay extends JavaPlugin {
    private static EusMapDisplay instance;

    private GlobalMapManager mapManager;

    private Gson gson = new GsonBuilder().create();

    @Override
    public void onEnable() {
        instance = this;

        File displayStorageFolder = new File(getDataFolder(), "/display");
        if (!displayStorageFolder.exists()) {
            displayStorageFolder.mkdirs();
        }

        DisplayStorage displayStorage = new DisplayStorage(gson, displayStorageFolder);

        this.mapManager = new GlobalMapManager(this, displayStorage);

        AdminCommand adminCommand = new AdminCommand(this);
        getCommand("mapdisplay").setExecutor(adminCommand);
        getCommand("mapdisplay").setTabCompleter(adminCommand);

        getServer().getPluginManager().registerEvents(new MapListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        mapManager.save();
    }


    public void sendSevere(String message) {
        Bukkit.getServer().getLogger().severe(prefixForEachLine(message));
    }

    public void sendWarn(String message) {
        Bukkit.getServer().getLogger().warning(prefixForEachLine(message));
    }

    public void sendInfo(String message) {
        Bukkit.getServer().getLogger().info(prefixForEachLine(message));
    }

    public String prefixForEachLine(String text) {
        String prefix = "[MapDisplay] ";
        String[] lines = text.split("\n");
        for (int i=0; i<lines.length; i++) {
            lines[i] = prefix + lines[i];
        }
        return String.join("\n", lines);
    }


    public GlobalMapManager getMapManager() {
        return mapManager;
    }

    public static EusMapDisplay getInstance() {
        return instance;
    }
}
