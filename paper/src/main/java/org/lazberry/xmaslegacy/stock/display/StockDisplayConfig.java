package org.lazberry.xmaslegacy.stock.display;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.stock.Stock;
import org.lazberry.xmaslegacy.stock.StockManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class StockDisplayConfig implements Initiator {
	private final XmasLegacy plugin;
	private final StockDisplayManager sdm;
	private final StockManager sm;
	private final File dataFolder;
	private @Getter YamlConfiguration config;
	private File file;

	@Inject
	public StockDisplayConfig(XmasLegacy plugin, StockDisplayManager sdm, StockManager sm) {
		this.plugin = plugin;
		this.sdm = sdm;
		this.sm = sm;
		this.dataFolder = plugin.getDataFolder();
	}

	@Override
	public void init() {
		file = new File(dataFolder, "stock_displays.yml");

		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			log.error("Failed to create directories for stock displays.");
			return;
		}

		if (!file.exists()) {
			try {
				if (file.createNewFile()) {
					log.info("Successfully created stock display files.");
				}
			} catch (IOException e) {
				log.error("Exception occurred while initiating stock display files.", e);
			}
		}

		createDefaultConfig();
		loadAll().join();
	}

	@Override
	public void close() {
		saveSync();
		sdm.clear();
	}

	private void createDefaultConfig() {
		config = YamlConfiguration.loadConfiguration(file);
	}

	public CompletableFuture<Void> saveAll() {
		return CompletableFuture.runAsync(this::saveSync);
	}

	public void saveSync() {
		synchronized (this) {
			config.set("displays", null);

			int index = 0;
			for (StockDisplay display : sdm.snapshot()) {
				String path = "displays." + index + ".";
				config.set(path + "location", display.getLocation());
				List<String> stockNames = new ArrayList<>();

				for (Stock stock : display.getStocks())
					stockNames.add(stock.getName());

				config.set(path + "stocks", stockNames);

				index++;
			}
		}
		saveConfig();
	}

	private void saveConfig() {
		try {
			config.save(file);
		} catch (IOException e) {
			log.error("Could not save stock displays config to {}", file, e);
		}
	}

	public Collection<StockDisplay> loadSync() {
		synchronized (this) {
			List<StockDisplay> result = new ArrayList<>();
			ConfigurationSection section = config.getConfigurationSection("displays");
			if (section == null) return result;

			for (String key : section.getKeys(false)) {
				String path = "displays." + key + ".";

				Location loc = config.getLocation(path + "location");
				List<String> stockNames = config.getStringList(path + "stocks");

				if (loc == null || stockNames.isEmpty()) continue;

				List<Stock> stockList = new ArrayList<>();
				for (String name : stockNames) {
					sm.getStock(name).ifPresent(stockList::add);
				}

				if (!stockList.isEmpty()) {
					StockDisplay display = new StockDisplay(loc, stockList.toArray(new Stock[0]));
					result.add(display);
					Bukkit.getScheduler().runTask(plugin, () -> sdm.add(display));
				}
			}
			return result;
		}
	}

	public CompletableFuture<Collection<StockDisplay>> loadAll() {
		return CompletableFuture.supplyAsync(this::loadSync);
	}
}

