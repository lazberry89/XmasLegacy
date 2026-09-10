package org.lazberry.xmaslegacy.system.temperature;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class WarmthManager implements Initiator {
    public record TempZone(String id, WarmthZone zone, long duration, long current) {}
    private final Map<String, WarmthZone> zones = new ConcurrentHashMap<>();
    private final Set<TempZone> temporaryZones = ConcurrentHashMap.newKeySet();
    private final XmasLegacy plugin;
    private volatile BukkitTask task;

    @Getter @Setter
    private volatile double bonusChance = 0.2d;
    @Getter @Setter
    private volatile double maxRegenerateSum = 10;

    @Inject
    public WarmthManager(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {

    }

    @Override
    public void close() {
        stopTask();
    }

    public void createWarmthZone(String id, Location loc1, Location loc2, WarmthLevel level) {
        zones.computeIfAbsent(id, i -> new WarmthZone(i, loc1, loc2, level, bonusChance, maxRegenerateSum));
    }

    public void createTemporaryZone(String id, Location loc1, Location loc2, WarmthLevel level, long expireAt) {
        WarmthZone zone = new WarmthZone(id, loc1, loc2, level, bonusChance, maxRegenerateSum);
        temporaryZones.add(new TempZone(id, zone, expireAt, System.currentTimeMillis()));
    }

    public void removeWarmthZone(String id) {
        zones.remove(id);
    }

    public void removeTemporaryZone(String id) {
        temporaryZones.removeIf(z -> Objects.equals(z.id(), id));
    }

    private void startTask() {
        if (task == null) synchronized (this) {
            if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (temporaryZones.isEmpty()) return;

                long now = System.currentTimeMillis();
                boolean result = temporaryZones.removeIf(t -> now - t.current() >= t.duration());
                if (result) log.warn("Temporary Warmth zone deleted.");
            }, 20L, 20L);
        }
    }

    private void stopTask() {
        if (task != null) synchronized (this) {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }
}
