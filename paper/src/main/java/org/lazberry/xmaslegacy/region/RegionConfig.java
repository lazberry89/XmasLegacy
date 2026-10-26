package org.lazberry.xmaslegacy.region;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Blocking;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class RegionConfig implements Initiator {
    private final File dataFolder;
    private YamlConfiguration config;
    private File file;

    @Inject
    public RegionConfig(XmasLegacy plugin) {
        this.dataFolder = plugin.getDataFolder();
    }

    @Override
    public void init() {
        if (!dataFolder.exists()) {
            if (dataFolder.mkdir()) {
                log.info("[Region] Data folder created.");
            } else {
                log.error("[Region] Failed to create data folder at: {}", dataFolder.getAbsolutePath());
            }
        }
        file = new File(dataFolder, "regions.yml");
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    log.info("[Region] regions.yml file created.");
                } else {
                    log.error("[Region] Failed to create regions.yml file.");
                }
            } catch (IOException e) {
                log.error("파일 생성 중 문제 발생: {}", e.getMessage(), e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Blocking
    public void saveSync(Map<UUID, List<Region>> regions) {
        synchronized (this) {
            var builder = ConfigBuilder.create();
            for (var entry : regions.entrySet()) {
                var uuid = entry.getKey();
                var regionList = entry.getValue();
                if (regionList.isEmpty()) continue;

                List<String> gsonValue = regionList.stream()
                        .map(Region::toGson)
                        .toList();
                builder.set(uuid.toString(), gsonValue);
            }
            this.config = builder.save(file).build();
        }
    }

    @Blocking
    public Map<UUID, List<Region>> loadSync() {
        synchronized (this) {
            this.config = YamlConfiguration.loadConfiguration(file);

            var section = config.getKeys(false);
            if (section.isEmpty()) return Map.of();

            Map<UUID, List<Region>> result = new HashMap<>();
            for (var key : section) {
                try {
                    UUID uuid = UUID.fromString(key);
                    List<?> loaded = config.getList(key);
                    if (loaded == null || loaded.isEmpty()) {
                        log.warn("Key {} exists, no key found. Skipping..", key);
                        continue;
                    }
                    List<Region> regions = loaded.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .map(Region::fromGson)
                            .filter(Objects::nonNull)
                            .toList();
                    result.put(uuid, regions);
                } catch (Exception e) {
                    log.error("Exception occurred while parsing String to Region instance. (key:{})", key, e);
                }
            }
            return result;
        }
    }

    public CompletableFuture<Void> saveAsync(Map<UUID, List<Region>> value) {
        return CompletableFuture.runAsync(() -> saveSync(value));
    }

    public CompletableFuture<Map<UUID, List<Region>>> loadAsync() {
        return CompletableFuture.supplyAsync(this::loadSync);
    }
}
