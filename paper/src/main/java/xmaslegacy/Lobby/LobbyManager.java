package xmaslegacy.Lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmaslegacy.XmasLegacy;

import java.util.concurrent.CompletableFuture;

public final class LobbyManager {
    private @Nullable Location spawn;
    private @NotNull final XmasLegacy plugin;

    public LobbyManager() {
        this.plugin = XmasLegacy.getInstance();
        load();
    }

    public void setSpawn(Location loc) {
        this.spawn = loc;
        save();
    }

    public void resetSpawn() {
        this.spawn = null;
        plugin.getConfig().set("lobby.spawn", null);
        plugin.saveConfig();
    }

    public @Nullable Location getSpawn() {
        return this.spawn;
    }
    public CompletableFuture<Void> save() {
        if (spawn == null) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            FileConfiguration config = plugin.getConfig();
            config.set("lobby.spawn.world", spawn.getWorld().getName());
            config.set("lobby.spawn.x", spawn.getX());
            config.set("lobby.spawn.y", spawn.getY());
            config.set("lobby.spawn.z", spawn.getZ());
            config.set("lobby.spawn.yaw", spawn.getYaw());
            config.set("lobby.spawn.pitch", spawn.getPitch());

            plugin.saveConfig();
        }, task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    private boolean load() {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("lobby.spawn")) return false;

        String worldName = config.getString("lobby.spawn.world");
        if (worldName == null) {
            plugin.getSLF4JLogger().error("로비 스폰 위치정보를 불러오는 도중 문제가 발생했습니다.");
            return false;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;

        double x = config.getDouble("lobby.spawn.x");
        double y = config.getDouble("lobby.spawn.y");
        double z = config.getDouble("lobby.spawn.z");
        float yaw = (float) config.getDouble("lobby.spawn.yaw");
        float pitch = (float) config.getDouble("lobby.spawn.pitch");

        this.spawn = new Location(world, x, y, z, yaw, pitch);
        return true;
    }

    public CompletableFuture<Boolean> reload() {
        return CompletableFuture.supplyAsync(() -> {
            plugin.reloadConfig();
            return load();
        }, task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }
}