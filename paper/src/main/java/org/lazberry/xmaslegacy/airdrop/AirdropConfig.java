package org.lazberry.xmaslegacy.airdrop;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;
import org.lazberry.xmaslegacy.utils.ParseEnum;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class AirdropConfig implements Initiator {
    private final File dataFolder;
    private @Getter YamlConfiguration config;
    private File file;

    @Inject
    public AirdropConfig(XmasLegacy plugin) {
        this.dataFolder = plugin.getDataFolder();
    }

    @Override
    public void init() {
        file = new File(dataFolder, "airdrop.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for airdrop settings.");
            return;
        }

        this.config = ConfigBuilder.of(file)
                .setDefault("drop-on-liquid", false)
                .setDefault("airdrop-material", "WAXED_COPPER_CHEST")
                .setDefault("process-tick", 1)
                .setDefault("supply-range", 1000)
                .setDefault("spawn-interval-ticks", 20 * 60 * 30)
                .save(file)
                .build();
        log.info("Successfully loaded all airdrop settings.");
    }

    public void saveCenterSync(Location loc) {
        synchronized (this) {
            this.config = ConfigBuilder.of(file)
                    .set("center-location", loc)
                    .save(file)
                    .build();
        }
    }

    public CompletableFuture<Void> saveCenterAsync(Location loc) {
        return CompletableFuture.runAsync(() -> saveCenterSync(loc));
    }

    public int getSpawnInterval() {
        return config.getInt("spawn-interval-ticks", 20 * 60 * 30);
    }

    public int getSupplyRange() {
        return config.getInt("supply-range", 1000);
    }

    public Location getCenterLocation() {
        return config.getLocation("center-location");
    }

    public boolean getDropOnLiquid() {
        return config.getBoolean("drop-on-liquid", false);
    }

    public Material getAirdropMaterial() {
        return ParseEnum.of(Material.class)
                .parseOrDefault(config.getString("airdrop-material"), Material.WAXED_COPPER_CHEST);
    }

    public int getTickPerProcess() {
        return config.getInt("process-tick", 1);
    }
}
