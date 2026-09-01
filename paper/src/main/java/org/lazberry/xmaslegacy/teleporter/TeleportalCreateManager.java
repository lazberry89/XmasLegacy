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

    public void setFirstLoc(Player p, Location loc) {
        firstLoc.put(p.getUniqueId(), loc);
    }

    public void setSecondLoc(Player p, Location loc) {
        secondLoc.put(p.getUniqueId(), loc);
    }

    public void setSpawnLoc(Player p, Location loc) {
        spawnLoc.put(p.getUniqueId(), loc);
    }

    public void clearSelection(Player p) {
        var uuid = p.getUniqueId();
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
