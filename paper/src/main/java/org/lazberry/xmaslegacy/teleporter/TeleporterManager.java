package org.lazberry.xmaslegacy.teleporter;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Registry.Include(type = ServerType.GLOBAL)
public class TeleporterManager implements Initiator {
	public static final NamespacedKey key = KeyUtils.get("teleporter");

    private @Getter @Setter long COOLDOWN_MILLIS = 3000L;
    private final Map<String, PortalEntry> teleporter = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldown = new ConcurrentHashMap<>();
    private final XmasLegacy plugin;
    private volatile BukkitTask task;

    @Inject
    public TeleporterManager(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        startEffectTask();
    }

	public ItemStack tool() {
		return ItemBuilder.of(plugin, Material.SPECTRAL_ARROW)
				.setTag(key, true)
				.setUnbreakable()
				.build();
	}

    public Collection<PortalEntry> snapshot() {
        return Collections.unmodifiableCollection(teleporter.values());
    }

    public boolean canTeleport(Location from, Location to) {
        if (from == null || to == null) return false;

        if (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ()) {
            return false;
        }

        if (getDestination(from).isPresent()) {
            return false;
        }

        return getDestination(to).isPresent();
    }

    public boolean registerWay(String id, Teleporter entrance, Location destination) {
        if (teleporter.containsKey(id)) return false;
        teleporter.put(id, new PortalEntry(id, entrance, destination));
        return true;
    }

    public Optional<Location> getDestination(Location loc) {
        if (loc == null) return Optional.empty();
        for (PortalEntry entry : teleporter.values()) {
            if (entry.entrance().isInside(loc)) {
                return Optional.of(entry.destination());
            }
        }
        return Optional.empty();
    }

    public boolean isCooldown(UUID uuid) {
        Long lastTime = cooldown.get(uuid);
        if (lastTime == null) return false;

        if (System.currentTimeMillis() - lastTime < COOLDOWN_MILLIS) {
            return true;
        }

        cooldown.remove(uuid);
        return false;
    }

    public void applyCooldown(UUID uuid) {
        cooldown.put(uuid, System.currentTimeMillis());
    }

    @Override
    public void close() {
        stopEffectTask();
    }

    public void startEffectTask() {
        if (task == null) synchronized (this) {
            if (task == null) {
                task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (teleporter.isEmpty()) return;
                    teleporter.values().forEach(t -> t.entrance().playParticleEffect());
                }, 5L, 5L);
            }
        }
    }

    public void stopEffectTask() {
        if (task != null) {
            synchronized (this) {
                if (task != null) {
                    task.cancel();
                    task = null;
                }
            }
        }
    }

    public boolean removeAt(Location loc) {
        return teleporter.values().removeIf(entry -> entry.entrance().isInside(loc));
    }

    public boolean remove(String id) {
        return teleporter.remove(id) != null;
    }

    public void clear() {
        teleporter.clear();
    }

    public record PortalEntry(String id, Teleporter entrance, Location destination) {}
}
