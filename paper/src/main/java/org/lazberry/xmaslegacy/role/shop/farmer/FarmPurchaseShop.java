package org.lazberry.xmaslegacy.role.shop.farmer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.role.shop.RoleShopManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class FarmPurchaseShop implements InventoryHolder {

	public record ShopRecord(Material material, int price) {}

	private final ShopRecord[] shopRecords = new ShopRecord[27];
	private final XmasLegacy plugin;
	private final Inventory inv;

	public FarmPurchaseShop(XmasLegacy plugin, RoleShopManager.Farmer farmer) {
		this.plugin = plugin;
		inv = Bukkit.createInventory(this, 27, ColorUtils.chat("농부상점"));

		var bg = InventoryHelper.background();
		for (int i = 0; i < inv.getSize(); i++) {
			inv.setItem(i, bg);
		}
		inv.setItem(0, InventoryHelper.back());

		inv.setItem(9, createBuyButton(9, Material.WHEAT_SEEDS, farmer.getWheatSeedPrice()));
		inv.setItem(10, createBuyButton(10, Material.PUMPKIN_SEEDS, farmer.getPumpkinSeedPrice()));
		inv.setItem(11, createBuyButton(11, Material.MELON_SEEDS, farmer.getMelonSeedPrice()));
		inv.setItem(12, createBuyButton(12, Material.WHEAT, farmer.getWheatPriceForShop()));
		inv.setItem(13, createBuyButton(13, Material.POTATO, farmer.getPotatoPriceForShop()));
		inv.setItem(14, createBuyButton(14, Material.CARROT, farmer.getCarrotPriceForShop()));
		inv.setItem(15, createBuyButton(15, Material.MELON_SLICE, farmer.getMelonSlicePriceForShop()));
		inv.setItem(16, createBuyButton(16, Material.PUMPKIN, farmer.getPumpkinPriceForShop()));
		inv.setItem(17, createBuyButton(17, Material.NETHER_WART, farmer.getNetherWartPriceForShop()));
	}

	public ShopRecord getRecord(int slot) {
		return shopRecords[slot];
	}

	private ItemStack createBuyButton(int slot, Material material, int price) {
		shopRecords[slot] = new ShopRecord(material, price);
		return ItemBuilder.of(plugin, material)
				.lore(
						ColorUtils.chat("&7아이콘을 클릭하여 구매하세요."),
						ColorUtils.chat("&7가격: &6" + price + "$")
				)
				.hideAllFlags().build();
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inv;
	}
}
