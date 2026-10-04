package org.lazberry.xmaslegacy.shop.goods;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class GoodsConfig implements Initiator {
	private final File dataFolder;
	private YamlConfiguration config;
	private File file;

	@Inject
	public GoodsConfig(XmasLegacy plugin) {
		this.dataFolder = plugin.getDataFolder();
	}

	@Override
	public void init() {
		file = new File(dataFolder, "goods.yml");

		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			log.error("Failed to create directories for goods.");
			return;
		}

		if (!file.exists()) {
			try {
				if (file.createNewFile()) {
					log.info("Successfully created goods files.");
				}
			} catch (IOException e) {
				log.error("Exception occurred while initiating goods files.", e);
			}
		}
		this.config = YamlConfiguration.loadConfiguration(file);
	}

	public void saveSync(Map<String, Goods> values) {
		synchronized (this) {
			var builder = ConfigBuilder.create();
			for (var goods : values.values()) {
				String path = "goods." + goods.getName() + ".";
				builder.set(path + "item", InventorySerializer.serializeContents(goods.getItem()));
				builder.set(path + "initial-price", goods.getInitialPrice());
			}
			this.config = builder.save(file).build();
		}
	}

	public Map<String, Goods> loadSync() {
		synchronized (this) {
			ConfigurationSection section = config.getConfigurationSection("goods");
			if (section == null) return new HashMap<>();

			Map<String, Goods> result = new HashMap<>();
			for (String key : section.getKeys(false)) {
				String path = "goods." + key + ".";
				ItemStack[] item;
				try {
					item = InventorySerializer.deserializeContents(config.getString(path + "item"));
				} catch (Exception e) {
					log.warn("Failed to load goods.{}", key, e);
					continue;
				}
				int price = config.getInt(path + "initial-price");
				Goods g = new Goods(key, item[0], price);
				result.put(key, g);
			}
			return result;
		}
	}

	public CompletableFuture<Void> saveAsync(Map<String, Goods> values) {
		return CompletableFuture.runAsync(() -> saveSync(values));
	}

	public CompletableFuture<Map<String, Goods>> loadAsync() {
		return CompletableFuture.supplyAsync(this::loadSync);
	}
}
