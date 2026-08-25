package org.lazberry.xmaslegacy.stock;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class StockManager {
	private final Map<String, Stock> stocks = new ConcurrentHashMap<>();
	private final StockUpdater updater = new StockUpdater();
	private final UserManager um;
	private final XmasLegacy plugin;
	private @Nullable StockItemBuilder builder = null;
	private double feeRate = 0.03;
	private Component icon = ColorUtils.chat("&#FF4545[&#F86E31주&#F1971D식&#EAC009]");
	private Material certificateItem;
	private World world = Bukkit.getWorld("port");
	private double totalSpread = 0.45;
	private double negativeOffset = 0.20;
	private boolean open = false;

	@Inject
	public StockManager(UserManager um, XmasLegacy plugin) {
        this.um = um;
        this.plugin = plugin;
    }

	public void setWorld(String name) {
		var world = Bukkit.getWorld(name);
		this.world = world == null ? Bukkit.getWorld("port") : world;
	}

	public boolean exists(String name) {
		return stocks.containsKey(name);
	}

	public boolean isStockCertificate(@Nullable ItemStack item) {
		if (item == null) return false;
		if (builder == null) builder = new StockItemBuilder(plugin, certificateItem);
		return builder.isStockItem(item);
	}

	public Optional<Stock> parseStockFromCertificate(@NotNull ItemStack item) {
		if (!isStockCertificate(item)) return Optional.empty();

		if (builder == null) builder = new StockItemBuilder(plugin, certificateItem);
		var value = KeyUtils.get(item, builder.getKeyStockId(), PersistentDataType.STRING);
		if (value == null || value.trim().isEmpty()) return Optional.empty();

		return getStock(value);
	}

	public Component icon() {
		return icon;
	}

	public Optional<Stock> getStock(@NotNull String name) {
		return Optional.ofNullable(stocks.get(name));
	}

	public Collection<Stock> getStocks() {
		return Collections.unmodifiableCollection(stocks.values());
	}

	@Contract("_, _, _, _ -> new")
	public Stock createStock(@NotNull String name, double initPrice, double maxPrice, double minPrice) {
		return registerStock(new Stock(name, initPrice, maxPrice, minPrice));
	}

	@Contract("_, _ -> new")
	public Stock createStock(@NotNull String name, double initPrice) {
		return registerStock(new Stock(name, initPrice));
	}

	public Stock registerStock(Stock stock) {
		stocks.put(stock.getName(), stock);
		return stock;
	}

	public <C extends Collection<Stock>> void registerAll(C values) {
		values.forEach(this::registerStock);
	}

	public double getChangeRate(Stock stock) {
		return stock.getChangeRate();
	}

	public @NotNull String getFormatStringMessage(@NotNull Stock stock) {
		return stock.getFormatStringMessage();
	}

	public @NotNull Component getFormatComponentMessage(@NotNull Stock stock) {
		return stock.getFormatComponentMessage();
	}

	@Contract(mutates = "this")
	@Flow(source = "this.stocks", targetIsContainer = true)
	public boolean removeStock(Stock stock) {
		return stocks.remove(stock.getName(), stock);
	}

	@Contract(mutates = "this")
	@Flow(source = "this.stocks", targetIsContainer = true)
	public @Nullable Stock removeStock(String name) {
		return stocks.remove(name);
	}

	public Response buyStock(@NotNull Player buyer, @NotNull Stock stock, int amount) {
		if (!isOpen()) return Response.TIMEOUT;
		var user = um.getUser(buyer.getUniqueId());
		if (user == null || amount <= 0) return Response.NOT_APPROPRIATE;

		double currentPrice = stock.getCurrentPrice();
		double totalCost = (currentPrice * amount) * (1 + feeRate);
		int finalCost = (int) Math.ceil(totalCost);

		if (user.getDollars() < finalCost) return Response.NOT_ENOUGH;
		user.addDollars(-finalCost);

		if (builder == null) builder = new StockItemBuilder(plugin, certificateItem);
		ItemStack cert = builder.createStockCertificate(stock, amount, currentPrice);
		buyer.getInventory().addItem(cert);
		return Response.SUCCESS;
	}

	public Response sellStock(Player owner, ItemStack certItem) {
		if (!isOpen()) return Response.TIMEOUT;
		if (builder == null) builder = new StockItemBuilder(plugin, certificateItem);

		var user = um.getUser(owner.getUniqueId());
		if (user == null || !builder.isStockItem(certItem)) return Response.NOT_APPROPRIATE;

		String stockId = KeyUtils.get(certItem, builder.getKeyStockId(), PersistentDataType.STRING);

		Stock stock = stocks.get(stockId);
		if (stock == null) return Response.NOT_EXIST;

		int amount = certItem.getAmount();
		double currentPrice = stock.getCurrentPrice();

		double totalRevenue = (currentPrice * amount) * (1 - feeRate);
		int finalRevenue = (int) Math.floor(totalRevenue);

		user.addDollars(finalRevenue);
		certItem.setAmount(0);

		return Response.SUCCESS;
	}

	public void updateAllPrices() {
		updater.tickPrices(stocks.values(), totalSpread, negativeOffset);
	}
}
