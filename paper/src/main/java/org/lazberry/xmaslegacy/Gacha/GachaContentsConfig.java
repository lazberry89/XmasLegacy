package org.lazberry.xmaslegacy.Gacha;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.ParseEnum;
import org.lazberry.xmaslegacy.Utils.InventorySerializer;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class GachaContentsConfig implements Initiator {
    private final GachaManager gm;
    private final File dataFolder;
    private @Getter YamlConfiguration config;
    private File file;

    public GachaContentsConfig(XmasLegacy plugin, GachaManager gm) {
        this.dataFolder = plugin.getDataFolder();
        this.gm = gm;
    }

    @Override
    public void init() {
        file = new File(dataFolder, "gacha-contents.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for gacha contents.");
            return;
        }

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    log.info("Successfully created gacha contents files.");
                }
            } catch (IOException e) {
                log.error("Exception occurred while initiating gacha contents files.", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadAll().join();
    }

    @Override
    public void close() {
        saveSync();
        gm.clear();
    }

    public void setInfo(Gacha gacha) {
        String path = "gacha." + gacha.getKey() + ".";
        config.set(path + "item", InventorySerializer.serializeContents(gacha.getItem()));
        config.set(path + "grade", gacha.getGrade().name());
        config.set(path + "chance", gacha.getChance());
    }

    public void saveSync() {
        synchronized (this) {
            config.set("gacha", null);
            gm.getAll().forEach(this::setInfo);

            try {
                config.save(file);
            } catch (IOException e) {
                log.error("Could not save gacha contents config to {}", file, e);
            }
        }
    }

    public CompletableFuture<Void> saveAll() {
        return CompletableFuture.runAsync(this::saveSync);
    }

    public void loadSync() {
        synchronized (this) {
            ConfigurationSection section = config.getConfigurationSection("gacha");
            if (section == null) return;

            for (String key : section.getKeys(false)) {
                String path = "gacha." + key + ".";

                ItemStack resultItem = null;
                try {
                    String serialized = config.getString(path + "item");
                    if (serialized != null) {
                        ItemStack[] items = InventorySerializer.deserializeContents(serialized);
                        if (items.length > 0) resultItem = items[0];
                    }
                } catch (Exception ignored) {}

                if (resultItem == null)
                    resultItem = new ItemStack(Material.BARRIER);

                GachaGrade grade = ParseEnum.of(GachaGrade.class).parseOrDefault(config.getString(path + "grade"), GachaGrade.NORMAL);
                double chance = config.getDouble(path + "chance", 0.0);

                gm.addGacha(key, resultItem, grade, chance);
            }
        }
    }

    public CompletableFuture<Void> loadAll() {
        return CompletableFuture.runAsync(this::loadSync);
    }
}
