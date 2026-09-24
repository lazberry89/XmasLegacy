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
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
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
        config.loadAll().whenComplete((l, e) -> {
            if (e == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    l.forEach(d -> {
                        deathBoxes.put(d.getLocation(), d);
                        boxQueue.add(d);
                    });
                    expireClearTask();
                });
            } else log.error("Failed to load death boxes from config.", e);
        });
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
                    while (!boxQueue.isEmpty()) {
                        DeathBox box = boxQueue.peek();
                        if (box != null && (box.isExpired() || box.isEmpty())) {
                            removeDeathBox(box);
                        } else break;
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
        var box = new DeathBox(dead, drops.toArray(ItemStack[]::new), expireTime, hideName);
        deathBoxes.put(box.getLocation(), box);
        boxQueue.add(box);

        drops.clear();
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