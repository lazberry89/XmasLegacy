package org.lazberry.xmaslegacy.role.general;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = ServerType.GLOBAL)
public class RoleConfig implements Initiator {
    private final RoleManager rm;
    private final File dataFolder;
    private File file;
    private @Getter YamlConfiguration config;

    @Inject
    public RoleConfig(RoleManager rm, XmasLegacy plugin) {
        this.rm = rm;
        this.dataFolder = plugin.getDataFolder();
    }

    @Override
    public void init() {
        file = new File(dataFolder, "roles.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for roles.yml");
            return;
        }

        boolean isNewFile = false;
        if (!file.exists()) {
            try {
                isNewFile = file.createNewFile();
            } catch (IOException e) {
                log.error("Could not create roles.yml", e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        if (isNewFile) {
            applyDefaults();
        }
        loadSync();
    }

    private void applyDefaults() {
        config = ConfigBuilder.of(file)
                // Farmer
                .setDefault("farmer.additional-drops.min", 1)
                .setDefault("farmer.additional-drops.max", 3)
                .setDefault("farmer.additional-exp.min", 3)
                .setDefault("farmer.additional-exp.max", 5)
                .setDefault("farmer.planting-exp", 1)
                .setDefault("farmer.planting-chance", 0.3)
                .setDefault("farmer.bonus-chance", 0.25)
                .setDefault("farmer.instant-growth-chance", 0.03)
                .setDefault("farmer.bonus-growth.min", 1)
                .setDefault("farmer.bonus-growth.max", 2)
                // Miner
                .setDefault("miner.exp.low.min", 2)
                .setDefault("miner.exp.low.max", 5)
                .setDefault("miner.exp.medium.min", 5)
                .setDefault("miner.exp.medium.max", 10)
                .setDefault("miner.exp.high.min", 10)
                .setDefault("miner.exp.high.max", 18)
                .setDefault("miner.exp.highest.min", 20)
                .setDefault("miner.exp.highest.max", 35)
                .setDefault("miner.exp.special.min", 40)
                .setDefault("miner.exp.special.max", 60)
                .setDefault("miner.search-radius", 2)
                .setDefault("miner.search-chance", 0.4)
                .setDefault("miner.glow-duration", 2)
                //Fisherman
                .setDefault("fisherman.exp.cod.min", 5)
                .setDefault("fisherman.exp.cod.max", 10)
                .setDefault("fisherman.exp.salmon.min", 10)
                .setDefault("fisherman.exp.salmon.max", 18)
                .setDefault("fisherman.exp.pufferfish.min", 20)
                .setDefault("fisherman.exp.pufferfish.max", 30)
                .setDefault("fisherman.exp.tropical-fish.min", 35)
                .setDefault("fisherman.exp.tropical-fish.max", 50)
                .setDefault("fisherman.exp.junk.min", 2)
                .setDefault("fisherman.exp.junk.max", 5)
                .setDefault("fisherman.exp.treasure.min", 40)
                .setDefault("fisherman.exp.treasure.max", 60)
                .setDefault("fisherman.cook-chance", 0.3)
                .setDefault("fisherman.junk-dollar.min", 1000)
                .setDefault("fisherman.junk-dollar.max", 2500)
                .setDefault("fisherman.treasure-dollar.min", 5000)
                .setDefault("fisherman.treasure-dollar.max", 15000)
                .save(file)
                .build();
    }

    public synchronized void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            log.error("Could not save roles.yml to {}", file, e);
        }
    }

    public void saveSync() {
        RoleManager.Farmer f = rm.farmer();
        RoleManager.Miner m = rm.miner();
        RoleManager.Fisherman fm = rm.fisherman();

        synchronized (this) {
            config.set("farmer.additional-drops.min", f.getAdditionalDropsMin());
            config.set("farmer.additional-drops.max", f.getAdditionalDropsMax());
            config.set("farmer.additional-exp.min", f.getAdditionalExpMin());
            config.set("farmer.additional-exp.max", f.getAdditionalExpMax());
            config.set("farmer.planting-exp", f.getPlantingExp());
            config.set("farmer.planting-chance", f.getPlantingExpChance());
            config.set("farmer.bonus-chance", f.getBonusGrowthChance());
            config.set("farmer.instant-growth-chance", f.getInstantMaxGrowthChance());
            config.set("farmer.bonus-growth.min", f.getBonusGrowthMin());
            config.set("farmer.bonus-growth.max", f.getBonusGrowthMax());

            // Miner
            config.set("miner.exp.low.min", m.getExpLowMin());
            config.set("miner.exp.low.max", m.getExpLowMax());
            config.set("miner.exp.medium.min", m.getExpMediumMin());
            config.set("miner.exp.medium.max", m.getExpMediumMax());
            config.set("miner.exp.high.min", m.getExpHighMin());
            config.set("miner.exp.high.max", m.getExpHighMax());
            config.set("miner.exp.highest.min", m.getExpHighestMin());
            config.set("miner.exp.highest.max", m.getExpHighestMax());
            config.set("miner.exp.special.min", m.getExpSpecialMin());
            config.set("miner.exp.special.max", m.getExpSpecialMax());
            config.set("miner.search-radius", m.getSearchRadius());
            config.set("miner.search-chance", m.getSearchChance());
            config.set("miner.glow-duration", m.getGlowDuration());

            // Fisherman
            config.set("fisherman.exp.cod.min", fm.getExpCodMin());
            config.set("fisherman.exp.cod.max", fm.getExpCodMax());
            config.set("fisherman.exp.salmon.min", fm.getExpSalmonMin());
            config.set("fisherman.exp.salmon.max", fm.getExpSalmonMax());
            config.set("fisherman.exp.pufferfish.min", fm.getExpPufferfishMin());
            config.set("fisherman.exp.pufferfish.max", fm.getExpPufferfishMax());
            config.set("fisherman.exp.tropical-fish.min", fm.getExpTropicalFishMin());
            config.set("fisherman.exp.tropical-fish.max", fm.getExpTropicalFishMax());
            config.set("fisherman.exp.junk.min", fm.getExpJunkMin());
            config.set("fisherman.exp.junk.max", fm.getExpJunkMax());
            config.set("fisherman.exp.treasure.min", fm.getExpTreasureMin());
            config.set("fisherman.exp.treasure.max", fm.getExpTreasureMax());
            config.set("fisherman.cook-chance", fm.getCookChance());
            config.set("fisherman.junk-dollar.min", fm.getJunkDollarMin());
            config.set("fisherman.junk-dollar.max", fm.getJunkDollarMax());
            config.set("fisherman.treasure-dollar.min", fm.getTreasureDollarMin());
            config.set("fisherman.treasure-dollar.max", fm.getTreasureDollarMax());

            saveConfig();
        }
    }

    public CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(this::saveSync);
    }

    public void loadSync() {
        synchronized (this) {
            this.config = YamlConfiguration.loadConfiguration(file);
            RoleManager.Farmer f = rm.farmer();
            RoleManager.Miner m = rm.miner();
            RoleManager.Fisherman fm = rm.fisherman();

            f.setAdditionalDropsMin(config.getInt("farmer.additional-drops.min", 1));
            f.setAdditionalDropsMax(config.getInt("farmer.additional-drops.max", 3));
            f.setAdditionalExpMin(config.getInt("farmer.additional-exp.min", 3));
            f.setAdditionalExpMax(config.getInt("farmer.additional-exp.max", 5));
            f.setPlantingExp(config.getInt("farmer.planting-exp", 1));
            f.setPlantingExpChance(config.getDouble("farmer.planting-chance", 0.3));
            f.setBonusGrowthChance(config.getDouble("farmer.bonus-chance", 0.25));
            f.setInstantMaxGrowthChance(config.getDouble("farmer.instant-growth-chance", 0.03));
            f.setBonusGrowthMin(config.getInt("farmer.bonus-growth.min", 1));
            f.setBonusGrowthMax(config.getInt("farmer.bonus-growth.max", 2));

            m.setExpLowMin(config.getInt("miner.exp.low.min", 2));
            m.setExpLowMax(config.getInt("miner.exp.low.max", 5));
            m.setExpMediumMin(config.getInt("miner.exp.medium.min", 5));
            m.setExpMediumMax(config.getInt("miner.exp.medium.max", 10));
            m.setExpHighMin(config.getInt("miner.exp.high.min", 10));
            m.setExpHighMax(config.getInt("miner.exp.high.max", 18));
            m.setExpHighestMin(config.getInt("miner.exp.highest.min", 20));
            m.setExpHighestMax(config.getInt("miner.exp.highest.max", 35));
            m.setExpSpecialMin(config.getInt("miner.exp.special.min", 40));
            m.setExpSpecialMax(config.getInt("miner.exp.special.max", 60));
            m.setSearchRadius(config.getInt("miner.search-radius", 2));
            m.setSearchChance(config.getDouble("miner.search-chance", 0.4));
            m.setGlowDuration(config.getInt("miner.glow-duration", 2));

            fm.setExpCodMin(config.getInt("fisherman.exp.cod.min", 5));
            fm.setExpCodMax(config.getInt("fisherman.exp.cod.max", 10));
            fm.setExpSalmonMin(config.getInt("fisherman.exp.salmon.min", 10));
            fm.setExpSalmonMax(config.getInt("fisherman.exp.salmon.max", 18));
            fm.setExpPufferfishMin(config.getInt("fisherman.exp.pufferfish.min", 20));
            fm.setExpPufferfishMax(config.getInt("fisherman.exp.pufferfish.max", 30));
            fm.setExpTropicalFishMin(config.getInt("fisherman.exp.tropical-fish.min", 35));
            fm.setExpTropicalFishMax(config.getInt("fisherman.exp.tropical-fish.max", 50));
            fm.setExpJunkMin(config.getInt("fisherman.exp.junk.min", 2));
            fm.setExpJunkMax(config.getInt("fisherman.exp.junk.max", 5));
            fm.setExpTreasureMin(config.getInt("fisherman.exp.treasure.min", 40));
            fm.setExpTreasureMax(config.getInt("fisherman.exp.treasure.max", 60));
            fm.setCookChance(config.getDouble("fisherman.cook-chance", 0.3));
            fm.setJunkDollarMin(config.getInt("fisherman.junk-dollar.min", 1000));
            fm.setJunkDollarMax(config.getInt("fisherman.junk-dollar.max", 2500));
            fm.setTreasureDollarMin(config.getInt("fisherman.treasure-dollar.min", 5000));
            fm.setTreasureDollarMax(config.getInt("fisherman.treasure-dollar.max", 15000));
        }
    }

    public CompletableFuture<Void> load() {
        return CompletableFuture.runAsync(this::loadSync);
    }

    @Override
    public void close() {
        saveSync();
    }
}