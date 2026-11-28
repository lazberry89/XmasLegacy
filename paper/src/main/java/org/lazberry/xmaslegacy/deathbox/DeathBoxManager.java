package org.lazberry.xmaslegacy.deathbox;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class DeathBoxManager implements Initiator {
    private final Map<Location, DeathBox> deathBoxes = new ConcurrentHashMap<>();
    private final Queue<DeathBox> boxQueue = new ConcurrentLinkedQueue<>();
    private final DeathBoxConfig config;
    private final XmasLegacy plugin;
    private volatile BukkitTask task;

    @Getter @Setter
    private long expireTime = 600_000L;

    @Getter @Setter
    private boolean hideName = false;

    @Getter @Setter
    private int boxCost = 12000;

    @Inject
    public DeathBoxManager(DeathBoxConfig config, XmasLegacy plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public void init() {
        setExpireTime(config.getExpireTime());
        setHideName(config.ifHideName());
        // On the main thread: restoring a box places its chest. Off it every box failed to load and was then
        // erased from the file on shutdown.
        config.loadSync().forEach(d -> {
            deathBoxes.put(d.getLocation(), d);
            boxQueue.add(d);
        });
        expireClearTask();
    }

    @Override
    public void close() {
        config.saveSync(queueSnapshot());

        stopTask();
        deathBoxes.clear();
        boxQueue.clear();
    }

    public void expireClearTask() {
        if (task == null) synchronized (this) {
            if (task == null) {
                task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    // All of them, not only the oldest: restored boxes have different times left.
                    for (DeathBox box : queueSnapshot()) {
                        if (box.isExpired() || box.isEmpty()) removeDeathBox(box);
                    }
                }, 0L, 20L);
            }
        }
    }

    public void stopTask() {
        if (task != null) synchronized (this) {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    public void dropDeathBox(Player dead, List<ItemStack> drops) {
        var box = new DeathBox(dead, freeSpot(dead.getLocation()), drops.toArray(ItemStack[]::new), expireTime, hideName);
        deathBoxes.put(box.getLocation(), box);
        boxQueue.add(box);

        drops.clear();
    }

    /** Never on top of another box or a container, and inside the world (deaths in the void). */
    private Location freeSpot(Location death) {
        var world = death.getWorld();
        int bottom = Math.clamp(death.getBlockY(), world.getMinHeight(), world.getMaxHeight() - 1);
        for (int y = bottom; y < Math.min(bottom + 16, world.getMaxHeight()); y++) {
            var block = world.getBlockAt(death.getBlockX(), y, death.getBlockZ());
            if (!deathBoxes.containsKey(block.getLocation()) && (block.isEmpty() || block.isReplaceable()))
                return block.getLocation();
        }
        for (int y = bottom; y < world.getMaxHeight(); y++) {
            var block = world.getBlockAt(death.getBlockX(), y, death.getBlockZ());
            if (!deathBoxes.containsKey(block.getLocation()) && !(block.getState(false) instanceof org.bukkit.block.TileState))
                return block.getLocation();
        }
        return world.getBlockAt(death.getBlockX(), bottom, death.getBlockZ()).getLocation();
    }

    public void removeDeathBox(Location loc) {
        removeDeathBox(deathBoxes.get(loc.getBlock().getLocation()));
    }

    public void removeDeathBox(DeathBox box) {
        if (box != null) {
            deathBoxes.remove(box.getLocation());
            boxQueue.remove(box);
            box.reset();
        }
    }

    public Optional<DeathBox> getDeathBox(Location loc) {
        return Optional.ofNullable(deathBoxes.get(loc.getBlock().getLocation()));
    }

    public List<DeathBox> queueSnapshot() {
        return new ArrayList<>(boxQueue);
    }
}