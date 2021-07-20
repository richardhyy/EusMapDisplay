package cc.eumc.eusmapdisplay.storage;

import cc.eumc.eusmapdisplay.model.Display;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;

public class DisplayStorage {
    private Map<UUID, MapDisplay> displays = new HashMap<>();

    private transient File storageFolder;
    private transient Gson gson;

    public DisplayStorage(Gson gson, File storageFolder) {
        this.gson = gson;
        this.storageFolder = storageFolder;
        load();
    }

    public void save() {
        displays.forEach((uuid, mapDisplay) -> {
            try {
                Files.writeString(new File(storageFolder, mapDisplay.getUniqueId().toString() + ".json").toPath(), gson.toJson(mapDisplay));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void load() {
        displays.clear();
        File[] files = storageFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null) {
            return;
        }

        for (File json : files) {
            try {
                UUID uuid = UUID.fromString(json.getName().substring(0, json.getName().indexOf(".")));
                displays.put(uuid, gson.fromJson(new JsonReader(new FileReader(json)), MapDisplay.class));
                System.out.println("Loaded MapDisplay: " + uuid);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public MapDisplay matchMapDisplay(Predicate<MapDisplay> predicate) {
        return displays.values().stream().filter(predicate).findFirst().orElse(null);
    }

    public MapDisplay getMapDisplay(UUID uuid) {
        return displays.get(uuid);
    }

    public void addMapDisplay(MapDisplay mapDisplay) {
        displays.put(mapDisplay.getUniqueId(), mapDisplay);
    }

    public void removeMapDisplay(MapDisplay mapDisplay) {
        displays.remove(mapDisplay.getUniqueId());
    }
}
