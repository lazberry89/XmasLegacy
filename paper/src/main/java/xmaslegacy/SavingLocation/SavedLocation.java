package xmaslegacy.SavingLocation;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmaslegacy.XmasLegacy;

import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class SavedLocation {
    private final @NotNull @Getter XmasLegacy plugin;
    private @Nullable @Getter Location spawn;
    private final @NotNull @Getter DestinationType type;

    public SavedLocation(@NotNull DestinationType type) {
        this.plugin = XmasLegacy.getInstance();
        this.type = type;
        load();
        log.warn("{} Location loaded.", type);
    }

    public void setSpawn(@NotNull Location loc) {
        this.spawn = loc;
        save().thenRun(() ->
            log.warn("{} Location set({}, {}, {}, ({}, {}))", type, loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw())
        );
    }

    public void resetSpawn() {
        this.spawn = null;
        CompletableFuture.runAsync(() -> {
            synchronized (plugin) {
                plugin.getConfig().set(type + ".spawn", null);
                plugin.saveConfig();
            }
        }, task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task)).thenRun(() -> {
            log.warn("{} Location reset.", type);
        });
    }

    public CompletableFuture<Boolean> reload() {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (plugin) {
                plugin.reloadConfig();
                return load();
            }
        }, task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    public @NotNull CompletableFuture<Void> save() {
        if (spawn == null) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            synchronized (plugin) {
                FileConfiguration config = plugin.getConfig();
                config.set(type + ".spawn.world", spawn.getWorld().getName());
                config.set(type + ".spawn.x", spawn.getX());
                config.set(type + ".spawn.y", spawn.getY());
                config.set(type + ".spawn.z", spawn.getZ());
                config.set(type + ".spawn.yaw", spawn.getYaw());
                config.set(type + ".spawn.pitch", spawn.getPitch());

                plugin.saveConfig();
            }
        }, task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    public boolean load() {
        synchronized (plugin) {
            FileConfiguration config = plugin.getConfig();
            if (!config.contains(type + ".spawn")) return false;

            String worldName = config.getString(type + ".spawn.world");
            if (worldName == null) {
                log.error("Error occurred while loading {} spawn location.", type);
                return false;
            }
            World world = Bukkit.getWorld(worldName);
            if (world == null) return false;

            double x = config.getDouble(type + ".spawn.x");
            double y = config.getDouble(type + ".spawn.y");
            double z = config.getDouble(type + ".spawn.z");
            float yaw = (float) config.getDouble(type + ".spawn.yaw");
            float pitch = (float) config.getDouble(type + ".spawn.pitch");

            this.spawn = new Location(world, x, y, z, yaw, pitch);
            return true;
        }
    }

    public @NotNull String formattedLocation() {
        if (this.spawn == null) return "";
        return String.format("world : %s, x: %.1f, y: %.1f, z: %.1f, yaw: %.1f, pitch: %.1f",
                this.spawn.getWorld(), this.spawn.getX(), this.spawn.getY(),
                this.spawn.getZ(), this.spawn.getYaw(), this.spawn.getPitch());
    }
}
