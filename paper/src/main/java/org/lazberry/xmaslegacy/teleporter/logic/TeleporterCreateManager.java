package org.lazberry.xmaslegacy.teleporter.logic;

import org.bukkit.Location;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Registry.Include(type = ServerType.GLOBAL)
public class TeleporterCreateManager {
    private final Map<UUID, Location> firstLoc = new HashMap<>();
    private final Map<UUID, Location> secondLoc = new HashMap<>();
    private final Map<UUID, Location> destination = new HashMap<>();

    public TeleporterCreateManager() {}

    public void setFirstLoc(UUID uuid, Location loc) {
        firstLoc.put(uuid, loc);
    }

    public void setSecondLoc(UUID uuid, Location loc) {
        secondLoc.put(uuid, loc);
    }

    public void setDestination(UUID uuid, Location loc) {
        destination.put(uuid, loc);
    }

    public void clearSelection(UUID uuid) {
        firstLoc.remove(uuid);
        secondLoc.remove(uuid);
        destination.remove(uuid);
    }

    public Location getFirstLoc(UUID uuid) {
        return firstLoc.get(uuid);
    }

    public Location getSecondLoc(UUID uuid) {
        return secondLoc.get(uuid);
    }

    public Location getDestination(UUID uuid) {
        return destination.get(uuid);
    }
}
