package org.lazberry.xmaslegacy.stock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ParseEnum;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class StockConfig implements Initiator {
	private final StockManager sm;
	private final File dataFolder;
	private @Getter YamlConfiguration config;
	private File file;

	@Inject
	public StockConfig(XmasLegacy plugin, StockManager sm) {
		this.dataFolder = plugin.getDataFolder();
		this.sm = sm;
	}

	@Override
	public void init() {
		file = new File(dataFolder, "stock_settings.yml");

		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			log.error("Failed to create directories for stock settings.");
			return;
		}

		this.config = ConfigBuilder.of(file)
				.setDefault("settings.icon", "&#FF4545[&#F86E31주&#F1971D식&#EAC009]")
				.setDefault("settings.certificate-item", "FLOW_BANNER_PATTERN")
				.setDefault("settings.target-world", "port")
				.setDefault("settings.total-spread", 0.45)
				.setDefault("settings.negative-offset", 0.20)
				.setDefault("settings.fee-rate", 0.03)
				.setDefault("settings.scheduler-interval", 20)
				.setDefault("settings.start-time.min", 0)
				.setDefault("settings.start-time.max", 12_000)
				.save(file)
				.build();

		applySettingsToManager();
		loadStocks().join();
		log.info("Successfully loaded all stock settings.");
	}

	@Override
	public void close() {

	}

	public CompletableFuture<Void> reloadConfig() {
		return CompletableFuture.runAsync(() -> {
			synchronized (this) {
				if (file != null) {
					config = YamlConfiguration.loadConfiguration(file);
					applySettingsToManager();
				}
			}
		});
	}

	private void applySettingsToManager() {
		sm.setFeeRate(getFeeRate());
		sm.setIcon(ColorUtils.chat(getIcon()));
		sm.setCertificateItem(getCertificateMaterial());
		sm.setTotalSpread(getTotalSpread());
		sm.setNegativeOffset(getNegativeOffset());
		sm.setWorld(getTargetWorldName());
	}

	public CompletableFuture<Void> saveSettings() {
		if (config == null || file == null) return CompletableFuture.completedFuture(null);

		return CompletableFuture.runAsync(() -> {
			synchronized (this) {
				saveConfig();
				applySettingsToManager();
			}
		});
	}

	public CompletableFuture<Collection<Stock>> loadStocks() {
		ConfigurationSection stocksSection = config.getConfigurationSection("stocks");
		if (stocksSection == null) return CompletableFuture.completedFuture(null);

		return CompletableFuture.supplyAsync(() -> {
			synchronized (this) {
				List<Stock> stocks = new ArrayList<>();
				for (String stockName : stocksSection.getKeys(false)) {
					String path = "stocks." + stockName + ".";
					double maxPrice = config.getDouble(path + "max-price", 91_000);
					double minPrice = config.getDouble(path + "min-price", 49_000);
					double initPrice = config.getDouble(path + "init-price", 70_000);
					double currentPrice = config.getDouble(path + "current-price", 70_000);
					double previousPrice = config.getDouble(path + "previous-price", 70_000);

					Stock stock = new Stock(stockName, initPrice, maxPrice, minPrice);
					stock.setCurrentPrice(currentPrice);
					stock.setPreviousPrice(previousPrice);
					stocks.add(stock);
					log.info("Loaded stock: {} (Current: {})", stockName, currentPrice);
				}
				sm.registerAll(stocks);
				return stocks;
			}
		});
	}

	public CompletableFuture<Void> saveStocks() {
		if (config == null || file == null) return CompletableFuture.completedFuture(null);
		return CompletableFuture.runAsync(() -> {
			synchronized (this) {
				sm.getStocks().forEach(this::setInfo);
				saveConfig();
			}
		});
	}

	public CompletableFuture<Stock> saveStock(@Nullable Stock stock) {
		if (config == null || file == null || stock == null) return CompletableFuture.completedFuture(null);
		return CompletableFuture.supplyAsync(() -> {
			synchronized (this) {
				setInfo(stock);
				saveConfig();
				return stock;
			}
		});
	}

	public void setInfo(@NotNull Stock stock) {
		String path = "stocks." + stock.getName() + ".";
		ConfigBuilder.of(config)
				.set(path + "max-price", stock.getMaxPrice())
				.set(path + "min-price", stock.getMinPrice())
				.set(path + "init-price", stock.getInitPrice())
				.set(path + "current-price", stock.getCurrentPrice())
				.set(path + "previous-price", stock.getPreviousPrice());
	}

	private void saveConfig() {
		try {
			config.save(file);
		} catch (IOException e) {
			log.error("Could not save stock config to {}", file, e);
		}
	}

	public String formatMinecraftTime(long ticks) {
		long hours = (ticks / 1000 + 6) % 24;
		long minutes = (ticks % 1000) * 60 / 1000;
		return String.format("%02d:%02d", hours, minutes);
	}

	public String getOperatingHoursFormatted() {
		String start = formatMinecraftTime(getMinimumStartTime());
		String end = formatMinecraftTime(getMaximumStartTime());
		return start + " ~ " + end;
	}

	public String getTargetWorldName() {
		return config.getString("settings.target-world", "world");
	}

	public double getFeeRate() {
		return config.getDouble("settings.fee-rate", 0.03);
	}

	public double getTotalSpread() {
		return config.getDouble("settings.total-spread", 0.45);
	}

	public double getNegativeOffset() {
		return config.getDouble("settings.negative-offset", 0.20);
	}

	public @NotNull Material getCertificateMaterial() {
		return ParseEnum.of(Material.class).parseOrDefault(config.getString("settings.certificate-item", "FLOW_BANNER_PATTERN"), Material.FLOW_BANNER_PATTERN);
	}

	public String getIcon() {
		return config.getString("settings.icon", "&#FF4545[&#F86E31주&#F1971D식&#EAC009]");
	}

	public long getSchedulerInterval() {
		return config.getLong("settings.scheduler-interval", 20L);
	}

	public int getMinimumStartTime() {
		return config.getInt("settings.start-time.min", 0);
	}

	public int getMaximumStartTime() {
		return config.getInt("settings.start-time.max", 12_000);
	}
}
