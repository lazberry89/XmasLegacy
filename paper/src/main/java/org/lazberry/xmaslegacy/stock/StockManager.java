package org.lazberry.xmaslegacy.stock;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.ParseUser;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Registry.Include(type = ServerType.GLOBAL)
public class StockManager {
	private final Map<String, Stock> stocks = new ConcurrentHashMap<>();
	private final StockUpdater updater = new StockUpdater();
	private final XmasLegacy plugin;
	private final double FEE_RATE = 0.03;

	public Component icon() {
		return ColorUtils.chat("&#FF4545[&#F86E31주&#F1971D식&#EAC009]");
	}

	@Inject
	public StockManager(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	public void registerStock(Stock stock) {
		stocks.put(stock.getName(), stock);
	}

	public boolean buyStock(Stock stock, int amount, @Nullable User user) {
		if (user == null || amount <= 0) return false;

		double currentPrice = stock.getCurrentPrice();
		double totalCost = (currentPrice * amount) * (1 + FEE_RATE);
		int finalCost = (int) Math.ceil(totalCost);

		if (user.getDollars() < finalCost) return false;
		user.addDollars(-finalCost);

		ItemStack cert = new StockItemBuilder(plugin).createStockCertificate(stock, amount, currentPrice);
		ParseUser.parse(user).whenComplete((u, e) -> {
			if (e == null && u != null && u.isOnline()) {
				u.getInventory().addItem(cert);
				InfoUtils.info(u, "구매하신 주식이 지급되었습니다.");
				InfoUtils.warn(u, "유저간 거래가 가능하지만, 분실시 책임은 본인에게 있습니다.");
			} else log.error("Item supply occurred error. {}", user.getName());
		});

		return true;
	}

	public boolean sellStock(ItemStack certItem, @Nullable User user, StockItemBuilder builder) {
		if (user == null || !builder.isStockItem(certItem)) return false;

		var meta = certItem.getItemMeta();
		String stockId = meta.getPersistentDataContainer().get(builder.getKeyStockId(), PersistentDataType.STRING);

		Stock stock = stocks.get(stockId);
		if (stock == null) return false;

		int amount = certItem.getAmount();
		double currentPrice = stock.getCurrentPrice();

		double totalRevenue = (currentPrice * amount) * (1 - FEE_RATE);
		int finalRevenue = (int) Math.floor(totalRevenue);

		user.addDollars(finalRevenue);
		certItem.setAmount(0);

		return true;
	}

	public void updateAllPrices() {
		updater.tickPrices(stocks.values());
	}
}
