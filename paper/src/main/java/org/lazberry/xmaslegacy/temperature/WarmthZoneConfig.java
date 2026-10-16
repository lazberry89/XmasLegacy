package org.lazberry.xmaslegacy.temperature;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;
import org.lazberry.xmaslegacy.utils.ParseEnum;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class WarmthZoneConfig implements Initiator {
    private final File dataFolder;
    private @Getter YamlConfiguration config;
    private File file;

	@Inject
    public WarmthZoneConfig(XmasLegacy plugin) {
        this.dataFolder = plugin.getDataFolder();
    }

    @Override
    public void init() {
        file = new File(dataFolder, "warmth_zone.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for warmth zones.");
            return;
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        log.info("Successfully loaded all warmth zones.");
    }

    public void saveSync(Collection<WarmthZone> zones) {
        var builder = ConfigBuilder.create();
        synchronized (this) {
            for (var zone : zones) {
                String key = zone.getId() + ".";
                builder.set(key + "world", zone.getWorld().getName());
                builder.set(key + "warmth-level", zone.getLevel());
                builder.set(key + "show-particle", zone.isShowParticle());
                builder.set(key + "regenerate", zone.isRegenerate());
            }
            builder.save(file);
            this.config = builder.build();
        }
    }

    public CompletableFuture<Void> saveAsync(Collection<WarmthZone> zones) {
        return CompletableFuture.runAsync(() -> saveSync(zones));
    }

    public Map<Long, WarmthZone> loadSync(double bonusChance, double maxRegenerateSum) {
        var builder = ConfigBuilder.of(config);

        Map<Long, WarmthZone> result = new HashMap<>();
        for (String key : config.getKeys(false)) {
            try {
                long id = Long.parseLong(key);
                World world = Bukkit.getWorld(builder.getValue(key + ".world", ""));
                WarmthLevel level = ParseEnum.of(WarmthLevel.class).parse(builder.getValue(key + ".warmth-level", ""));
                boolean showParticle = builder.getValue(key + ".show-particle", false);
                boolean regenerate = builder.getValue(key + ".regenerate", true);

                if (world == null || level == null) {
                    log.error("Failed to load warmth zone from config. Skipping that instance..");
                    continue;
                }
                var zone = new WarmthZone(world.getChunkAt(id), level, bonusChance, maxRegenerateSum);
                zone.setShowParticle(showParticle);
                zone.setRegenerate(regenerate);

                result.put(id, zone);
            } catch (NumberFormatException e) {
                log.error("Failed to load warmth zone due to NumberFormatException. Zone's id is not valid! (id: {})", key, e);
            }
        }
        return result;
    }

    public CompletableFuture<Map<Long, WarmthZone>> loadAsync(double bonusChance, double maxRegenerateSum) {
        return CompletableFuture.supplyAsync(() -> loadSync(bonusChance, maxRegenerateSum));
    }
}
