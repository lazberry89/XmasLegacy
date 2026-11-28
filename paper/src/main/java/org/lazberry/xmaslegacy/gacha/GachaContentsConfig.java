package org.lazberry.xmaslegacy.gacha;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.utils.ParseEnum;
import org.lazberry.xmaslegacy.utils.InventorySerializer;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class GachaContentsConfig implements Initiator {
    private final GachaManager gm;
    private final File dataFolder;
    private @Getter YamlConfiguration config;
    private File file;

	@Inject
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

    public void setInfo(BundleType type, Gacha gacha) {
    String path = "gacha." + type.name() + "." + gacha.getKey() + ".";
        config.set(path + "item", InventorySerializer.serializeContents(gacha.getItem()));
        config.set(path + "grade", gacha.getGrade().name());
        config.set(path + "chance", gacha.getChance());
    }

    public void saveSync() {
        synchronized (this) {
            config.set("gacha", null);
            for (BundleType type : BundleType.values())
            	gm.getAllSortedByChance(type).forEach(gacha -> setInfo(type, gacha));

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

            // Files written before the bundle type was saved have the items directly under "gacha".
            loadItems(section, BundleType.NORMAL);
            for (BundleType type : BundleType.values()) {
            	ConfigurationSection items = section.getConfigurationSection(type.name());
            	if (items != null) loadItems(items, type);
            }
            		}
            	}

            	private void loadItems(ConfigurationSection section, BundleType type) {
		for (String key : section.getKeys(false)) {
			if (!section.isString(key + ".item")) continue;
			ItemStack resultItem = null;
			try {
				ItemStack[] items = InventorySerializer.deserializeContents(section.getString(key + ".item"));
				if (items.length > 0) resultItem = items[0];
			} catch (Exception e) {
				log.warn("Failed to read gacha item {}", key, e);
			}
			if (resultItem == null)
				resultItem = new ItemStack(Material.BARRIER);

			GachaGrade grade = ParseEnum.of(GachaGrade.class).parseOrDefault(section.getString(key + ".grade"), GachaGrade.NORMAL);
			double chance = section.getDouble(key + ".chance", 0.0);
			gm.addGacha(key, resultItem, grade, chance, type);
		}
	}

	public CompletableFuture<Void> loadAll() {
        return CompletableFuture.runAsync(this::loadSync);
    }
}
