package org.lazberry.xmaslegacy.stock;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;

@Getter
public class StockItemBuilder {
	private final NamespacedKey keyStockId;
	private final NamespacedKey keyBuyPrice;
	private final XmasLegacy plugin;

	public StockItemBuilder(XmasLegacy plugin) {
		this.plugin = plugin;
		this.keyStockId = KeyUtils.get("stock_id");
		this.keyBuyPrice = KeyUtils.get("buy_price");
	}

	public ItemStack createStockCertificate(Stock stock, int amount, double buyPrice) {
		return ItemBuilder.of(plugin, Material.FLOW_BANNER_PATTERN)
				.setName(ColorUtils.chat("&#FF4545[&#F86E31주&#F1971D식&#EAC009]" + stock.getName()))
				.setLore(
						ColorUtils.chat("&7매수 단가: " + (int) buyPrice + "$"),
						ColorUtils.chat("&7수량: 1주")
				)
				.setGlint(true)
				.setMaxStackSize(1)
				.setTag(keyStockId, stock.getName())
				.setTag(keyBuyPrice, buyPrice)
				.setAmount(amount)
				.build();
	}

	public boolean isStockItem(ItemStack item) {
		if (item == null || !item.hasItemMeta()) return false;
		return item.getItemMeta().getPersistentDataContainer().has(keyStockId, PersistentDataType.STRING);
	}

}
