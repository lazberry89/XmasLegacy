package org.lazberry.xmaslegacy.region;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Blocking;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class RegionConfig implements Initiator {
    private final File dataFolder;
    private final File file;
    private YamlConfiguration config;
    /** Regions whose world is not loaded; written back unchanged instead of being lost. */
    private final Map<UUID, List<String>> unresolved = new HashMap<>();

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
            Map<UUID, List<String>> all = new HashMap<>();
            unresolved.forEach((uuid, list) -> all.put(uuid, new ArrayList<>(list)));
            for (var entry : regions.entrySet()) {
            	if (entry.getValue() == null) continue;
            	for (Region region : entry.getValue()) {
            		if (region != null) all.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(region.toGson());
            	}
            }
            all.forEach((uuid, list) -> builder.set(uuid.toString(), list));
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
        unresolved.clear();
        var section = config.getKeys(false);
        if (section.isEmpty()) return new HashMap<>();

        Map<UUID, List<Region>> result = new HashMap<>();
        for (var key : section) {
            try {
                UUID uuid = UUID.fromString(key);
                List<?> loaded = config.getList(key);
                if (loaded == null || loaded.isEmpty()) continue;

                List<Region> regions = new ArrayList<>();
                for (Object raw : loaded) {
                	if (!(raw instanceof String json)) continue;
                	Region region = Region.fromGson(json);
                	if (region != null && region.isValid()) regions.add(region);
                	else unresolved.computeIfAbsent(uuid, k -> new ArrayList<>()).add(json);
                }

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
