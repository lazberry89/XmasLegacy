package org.lazberry.xmaslegacy.stock.shop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.stock.Stock;
import org.lazberry.xmaslegacy.stock.StockManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.*;

@Registry.Include(type = ServerType.MAIN)
public class StockShopManager {
	private final StockManager sm;
	private final XmasLegacy plugin;
	private final StockShop shop;

	@Inject
	public StockShopManager(StockManager sm, XmasLegacy plugin) {
		this.sm = sm;
		this.plugin = plugin;
		this.shop = new StockShop(Collections.emptyList());
	}

	public void updateShop() {
		var builder = sm.getBuilder();
		List<ItemStack> currentStockItems = sm.getStocks().stream()
				.filter(Objects::nonNull)
				.map(builder::createStockShowItem)
				.toList();

		Inventory inv = this.shop.getInventory();

		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack item = (i < currentStockItems.size()) ? currentStockItems.get(i) : null;
			inv.setItem(i, item);
		}

		for (var human : List.copyOf(inv.getViewers())) {
			if (human instanceof Player p) {
				p.updateInventory();
			}
		}
	}

	public StockShop openShop(Player viewer) {
		updateShop();
		viewer.openInventory(shop.getInventory());
		return shop;
	}

	public void select(Player viewer, ItemStack selected) {
		Optional<Stock> optional = sm.parseStockFromCertificate(selected);
		if (optional.isEmpty()) {
			InfoUtils.error(viewer, "유효하지 않은 주식입니다.");
			return;
		}
		Stock stock = optional.get();
		var result = new StockSelectedShop(stock, sm.getBuilder(), plugin);

		viewer.openInventory(result.getInventory());
	}

	public void updateSelectionInv() {
		Bukkit.getOnlinePlayers().forEach(p -> {
			if (p.getOpenInventory().getTopInventory().getHolder() instanceof StockSelectedShop sel) {
				sel.update();
				p.updateInventory();
			}
		});
	}
}