package xmasLegacy.Lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;
import xmasLegacy.XmasLegacy;

public class LobbyManager {
    private Location spawn;
    private final XmasLegacy plugin;

    public LobbyManager(XmasLegacy plugin) {
        this.plugin = plugin;
        load();
    }

    public void setSpawn(Location loc) {
        this.spawn = loc;
        save();
    }

    public boolean resetSpawn() {
        if (spawn == null) return false;
        this.spawn = null;
        plugin.getConfig().set("lobby.spawn", null);
        plugin.saveConfig();
        return true;
    }

    public @Nullable Location getSpawn() {
        return this.spawn;
    }

    private void save() {
        if (spawn == null) return;
        FileConfiguration config = plugin.getConfig();
        config.set("lobby.spawn.world", spawn.getWorld().getName());
        config.set("lobby.spawn.x", spawn.getX());
        config.set("lobby.spawn.y", spawn.getY());
        config.set("lobby.spawn.z", spawn.getZ());
        config.set("lobby.spawn.yaw", spawn.getYaw());
        config.set("lobby.spawn.pitch", spawn.getPitch());
        plugin.saveConfig();
    }

    private void load() {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("lobby.spawn")) return;

        String worldName = config.getString("lobby.spawn.world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        double x = config.getDouble("lobby.spawn.x");
        double y = config.getDouble("lobby.spawn.y");
        double z = config.getDouble("lobby.spawn.z");
        float yaw = (float) config.getDouble("lobby.spawn.yaw");
        float pitch = (float) config.getDouble("lobby.spawn.pitch");

        this.spawn = new Location(world, x, y, z, yaw, pitch);
    }
}