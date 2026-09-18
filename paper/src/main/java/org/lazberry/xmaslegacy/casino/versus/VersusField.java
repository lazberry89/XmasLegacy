package org.lazberry.xmaslegacy.casino.versus;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Data
@ConsumableClass
public class VersusField {
    private final XmasLegacy plugin;
    private final Location blueSpawn;
    private final Location redSpawn;
    private final Location blueWaitingRoom;
    private final Location redWaitingRoom;
    private final Location entrance;
    private final Set<Location> breakableFences = ConcurrentHashMap.newKeySet();
    private final Set<Location> spectatorSpawn = ConcurrentHashMap.newKeySet();
    private UUID redFighter;
    private UUID blueFighter;
    private boolean running;
    private Material restoreMaterial = Material.IRON_BARS;

    public VersusField(Location blueSpawn, Location redSpawn, Location blueWaitingRoom, Location redWaitingRoom, Location entrance, XmasLegacy plugin) {
        this.plugin = plugin;
        this.blueSpawn = blueSpawn;
        this.redSpawn = redSpawn;
        this.blueWaitingRoom = blueWaitingRoom;
        this.redWaitingRoom = redWaitingRoom;
        this.entrance = entrance;
    }

    public boolean teleportWaitingRoom() {
        if (isFull()) {
            var p1 = Bukkit.getPlayer(blueFighter);
            var p2 = Bukkit.getPlayer(redFighter);

            if (p1 == null || p2 == null) return false;
            p1.teleport(blueWaitingRoom);
            p2.teleport(redWaitingRoom);
            return true;
        }
        return false;
    }

    public boolean isFull() {
        return redFighter != null && blueFighter != null;
    }

    public boolean addFence(Location loc) {
        return breakableFences.add(loc);
    }

    public boolean removeFence(Location loc) {
        return breakableFences.remove(loc);
    }

    public void breakSequentially() {
        if (breakableFences.isEmpty()) return;
        int i = 0;
        for (Location loc : breakableFences) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                World world = loc.getWorld();
                if (world == null || world.getBlockAt(loc).getType().isAir()) return;
                world.setType(loc, Material.AIR);
            }, i);
            i = i + 2;
        }
    }

    public void restoreFences() {
        breakableFences.forEach(l -> l.getWorld().setType(l, restoreMaterial));
    }

    public boolean isFighter(UUID uuid) {
        return uuid.equals(redFighter) || uuid.equals(blueFighter);
    }

    public boolean leave(UUID uuid) {
        if (uuid.equals(blueFighter)) blueFighter = null;
        else if (uuid.equals(redFighter)) redFighter = null;
        else return false;
        return true;
    }

    public boolean joinRandomly(UUID uuid) {
        if (isFull()) return false;
        if (uuid.equals(redFighter) || uuid.equals(blueFighter)) return false;

        if (ThreadLocalRandom.current().nextBoolean()) {
            if (redFighter == null) redFighter = uuid;
            else blueFighter = uuid;
        } else {
            if (blueFighter == null) blueFighter = uuid;
            else redFighter = uuid;
        }
        return true;
    }
}
