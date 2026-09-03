package org.lazberry.xmaslegacy.teleporter;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Registry.Include(type = ServerType.GLOBAL)
public class TeleportalCreateManager {
    private final Map<UUID, Location> firstLoc = new HashMap<>();
    private final Map<UUID, Location> secondLoc = new HashMap<>();
    private final Map<UUID, Location> spawnLoc = new HashMap<>();

    public TeleportalCreateManager() {}

    public void setFirstLoc(UUID uuid, Location loc) {
        firstLoc.put(uuid, loc);
    }

    public void setSecondLoc(UUID uuid, Location loc) {
        secondLoc.put(uuid, loc);
    }

    public void setSpawnLoc(UUID uuid, Location loc) {
        spawnLoc.put(uuid, loc);
    }

    public void clearSelection(UUID uuid) {
        firstLoc.remove(uuid);
        secondLoc.remove(uuid);
        spawnLoc.remove(uuid);
    }

    public Location getFirstLoc(UUID uuid) {
        return firstLoc.get(uuid);
    }

    public Location getSecondLoc(UUID uuid) {
        return secondLoc.get(uuid);
    }

    public Location getSpawnLoc(UUID uuid) {
        return spawnLoc.get(uuid);
    }
}
