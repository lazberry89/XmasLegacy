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
		int count = sdm.cleanDisplays(sm.getWorld());
		log.info("{} displays were cleaned.", count);
		// loadAll: config 읽기(비동기) → spawn(메인스레드) 순서로 실행됨
		// join()은 메인스레드 블로킹 + 메인스레드에서 spawn 대기 → 데드락 위험으로 제거
		loadAll().whenComplete((result, ex) -> {
			if (ex != null) log.error("Failed to load stock displays.", ex);
			else log.info("Stock displays load completed: {} entries scheduled.", result == null ? 0 : result.size());
		});
	}

	@Override
	public void close() {
		saveSync();
		sdm.clearSync();
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
					result.add(new StockDisplay(loc, stockList.toArray(new Stock[0])));
				}
			}
			return result;
		}
	}

	public CompletableFuture<Collection<StockDisplay>> loadAll() {
		// 1. config 읽기는 비동기에서 수행
		return CompletableFuture.supplyAsync(this::loadSync)
				// 2. 실제 spawn(sdm.addAll)은 반드시 메인 스레드에서 수행
				.thenApplyAsync(loaded -> {
					sdm.addAll(loaded);
					log.info("Loaded and spawned {} stock displays.", loaded.size());
					return loaded;
				}, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
	}
}

