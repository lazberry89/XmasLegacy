package org.lazberry.xmaslegacy.collectors.backup;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;
import org.lazberry.xmaslegacy.utils.InventorySerializer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class CollectorsConfig implements Initiator {
    private final File dataFolder;
    private YamlConfiguration config;
    private File file;

    @Inject
    public CollectorsConfig(XmasLegacy plugin) {
        this.dataFolder = plugin.getDataFolder();
    }

    @Override
    public void init() {
        file = new File(dataFolder, "collectors_backup.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for collectors_backup.yml");
            return;
        }

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    log.info("Successfully created collectors_backup.yml");
                }
            } catch (IOException e) {
                log.error("Exception occurred while initiating collectors_backup", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public CompletableFuture<Void> removeBackup(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                config.set(uuid.toString(), null);
                ConfigBuilder.of(config).save(file);
            }
        }).exceptionally(throwable -> {
            log.error("Failed to remove backup for UUID: {}", uuid, throwable);
            return null;
        });
    }

    public synchronized void saveSync(Map<UUID, String> serialized) {
        var builder = ConfigBuilder.of(config);
        serialized.forEach((uuid, data) -> builder.set(uuid.toString(), data));

        config = builder.save(file).build();
    }

    public CompletableFuture<Void> save(Player... players) {
        List<Player> validPlayers = Arrays.stream(players)
                .filter(p -> p != null && p.isValid())
                .toList();

        List<UUID> uuids = validPlayers.stream()
                .map(Player::getUniqueId)
                .toList();

        List<String> serialized = validPlayers.stream()
                .map(p -> p.getInventory().getContents())
                .map(InventorySerializer::serializeContents)
                .toList();

        return CompletableFuture.runAsync(() -> {
            Map<UUID, String> dataMap = new HashMap<>();
            for (int i = 0; i < uuids.size(); i++) {
                dataMap.put(uuids.get(i), serialized.get(i));
            }

            synchronized (this) {
                saveSync(dataMap);
            }
        }).exceptionally(throwable -> {
            log.error("Failed to save player inventory backups asynchronously", throwable);
            return null;
        });
    }

    public synchronized Map<UUID, ItemStack[]> loadSync() {
        Map<UUID, ItemStack[]> result = new HashMap<>();
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                log.error("Failed to find uuid. UUID might be damaged.", e);
                continue;
            }
            ItemStack[] contents;
            try {
                contents = InventorySerializer.deserializeContents(config.getString(uuidStr));
            } catch (Exception e) {
                log.error("Failed to deserialize item from Base64. File might be damaged.", e);
                continue;
            }
            result.put(uuid, contents);
        }
        return result;
    }

    public CompletableFuture<Map<UUID, ItemStack[]>> load() {
        return CompletableFuture.supplyAsync(this::loadSync);
    }
}
