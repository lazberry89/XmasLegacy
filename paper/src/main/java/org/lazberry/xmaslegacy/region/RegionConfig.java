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
    private final File file;
    private YamlConfiguration config;

    @Inject
    public RegionConfig(XmasLegacy plugin) {
        this.dataFolder = plugin.getDataFolder();
        this.file = new File(dataFolder, "regions.yml");
    }

    @Override
    public void init() {
        ensureFile();
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    private synchronized void ensureFile() {
        if (!dataFolder.exists()) {
            if (dataFolder.mkdirs()) {
                log.info("[Region] Data folder created.");
            }
        }
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    log.info("[Region] regions.yml file created.");
                }
            } catch (IOException e) {
                log.error("[Region] Failed to create regions.yml file: {}", e.getMessage(), e);
            }
        }
    }

    @Blocking
    public synchronized void saveSync(Map<UUID, List<Region>> regions) {
        try {
            ensureFile();
            var builder = ConfigBuilder.create();
            for (var entry : regions.entrySet()) {
                var uuid = entry.getKey();
                var regionList = entry.getValue();
                if (regionList == null || regionList.isEmpty()) continue;

                List<String> gsonValue = regionList.stream()
                        .filter(Objects::nonNull)
                        .map(Region::toGson)
                        .toList();
                builder.set(uuid.toString(), gsonValue);
            }
            this.config = builder.save(file).build();
            log.info("[Region] Successfully saved {} user region entries to regions.yml", regions.size());
        } catch (Exception e) {
            log.error("[Region] Failed to save regions to file!", e);
        }
    }

    @Blocking
    public synchronized Map<UUID, List<Region>> loadSync() {
        ensureFile();
        this.config = YamlConfiguration.loadConfiguration(file);

        var section = config.getKeys(false);
        if (section.isEmpty()) return new HashMap<>();

        Map<UUID, List<Region>> result = new HashMap<>();
        for (var key : section) {
            try {
                UUID uuid = UUID.fromString(key);
                List<?> loaded = config.getList(key);
                if (loaded == null || loaded.isEmpty()) continue;

                List<Region> regions = new ArrayList<>(loaded.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(Region::fromGson)
                        .filter(r -> r != null && r.isValid())
                        .toList());

                if (!regions.isEmpty()) {
                    result.put(uuid, regions);
                }
            } catch (Exception e) {
                log.error("[Region] Exception occurred while parsing String to Region instance. (key:{})", key, e);
            }
        }
        log.info("[Region] Successfully loaded {} user region entries from regions.yml", result.size());
        return result;
    }

    public CompletableFuture<Void> saveAsync(Map<UUID, List<Region>> value) {
        return CompletableFuture.runAsync(() -> saveSync(value));
    }

    public CompletableFuture<Map<UUID, List<Region>>> loadAsync() {
        return CompletableFuture.supplyAsync(this::loadSync);
    }
}
