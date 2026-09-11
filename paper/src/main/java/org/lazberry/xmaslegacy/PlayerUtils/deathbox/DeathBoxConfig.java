package org.lazberry.xmaslegacy.PlayerUtils.deathbox;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class DeathBoxConfig implements Initiator {
    private final File dataFolder;
    private YamlConfiguration config;
    private File file;

    @Inject
    public DeathBoxConfig(XmasLegacy plugin) {
        this.dataFolder = plugin.getDataFolder();
    }

    @Override
    public void init() {
        file = new File(dataFolder, "death_box.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for DeathBoxes.");
            return;
        }

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    log.info("Successfully created death_box.yml files.");
                }
            } catch (IOException e) {
                log.error("Exception occurred while initiating death_box.yml files.", e);
            }
        }
        settingDefaults();
    }

    private void settingDefaults() {
        this.config = ConfigBuilder.of(file)
                .setDefault("expire_time", 600000)
                .setDefault("hide_name", false)
                .setDefault("box_cost", 12000)
                .save(file)
                .build();
    }

    public int getBoxCost() {
        return config.getInt("box_cost", 12000);
    }

    public long getExpireTime() {
        return config.getLong("expire_time", 600_000L);
    }

    public boolean ifHideName() {
        return config.getBoolean("hide_name", false);
    }

    public void saveSync(List<DeathBox> savedQueue) {
        synchronized (this) {
            List<Map<String, Object>> serializedList = new ArrayList<>();

            for (DeathBox box : savedQueue) {
                if (box.isExpired() || box.getRemainingTime() <= 0) {
                    box.reset();
                    continue;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("uuid", box.getUuid().toString());
                data.put("world", box.getWorld().getName());
                data.put("x", box.getLocation().getBlockX());
                data.put("y", box.getLocation().getBlockY());
                data.put("z", box.getLocation().getBlockZ());
                data.put("previous_block", box.getPreviousBlock().name());
                data.put("remaining_time", box.getRemainingTime());
                data.put("contents", box.getContents());

                serializedList.add(data);
            }

            this.config = ConfigBuilder.of(file)
                    .set("death_boxes", serializedList)
                    .save(file)
                    .build();
        }
    }

    public CompletableFuture<Void> saveAll(List<DeathBox> savedQueue) {
        return CompletableFuture.runAsync(() -> saveSync(savedQueue));
    }

    public List<DeathBox> loadSync() {
        synchronized (this) {
            List<DeathBox> loadedBoxes = new ArrayList<>();
            this.config = YamlConfiguration.loadConfiguration(file);

            List<Map<?, ?>> rawList = this.config.getMapList("death_boxes");
            if (rawList.isEmpty()) return loadedBoxes;

            for (Map<?, ?> data : rawList) {
                try {
                    String worldName = (String) data.get("world");
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;

                    UUID uuid = UUID.fromString((String) data.get("uuid"));
                    int x = ((Number) data.get("x")).intValue();
                    int y = ((Number) data.get("y")).intValue();
                    int z = ((Number) data.get("z")).intValue();
                    Location loc = new Location(world, x, y, z);

                    Material previousBlock = Material.valueOf((String) data.get("previous_block"));
                    long remainingTime = ((Number) data.get("remaining_time")).longValue();

                    @SuppressWarnings("unchecked")
                    List<ItemStack> contents = (List<ItemStack>) data.get("contents");

                    String ownerName = Bukkit.getOfflinePlayer(uuid).getName();
                    if (ownerName == null) ownerName = "Unknown";

                    DeathBox box = new DeathBox(uuid, loc, previousBlock, contents, remainingTime, ownerName);
                    loadedBoxes.add(box);

                } catch (Exception e) {
                    log.error("Failed to parse death box data: {}", data, e);
                }
            }
            return loadedBoxes;
        }
    }

    public CompletableFuture<List<DeathBox>> loadAll() {
        return CompletableFuture.supplyAsync(this::loadSync);
    }
}
