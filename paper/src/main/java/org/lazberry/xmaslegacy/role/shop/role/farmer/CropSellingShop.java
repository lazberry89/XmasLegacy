package org.lazberry.xmaslegacy.role.shop.role.farmer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.role.shop.role.RoleShop;
import org.lazberry.xmaslegacy.role.shop.role.RoleShopManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

public class CropSellingShop extends RoleShop implements InventoryHolder {
	private final XmasLegacy plugin;
	private final Inventory inv;

	public CropSellingShop(XmasLegacy plugin, RoleShopManager.Farmer farmer) {
		this.plugin = plugin;
		this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("작물판매"));

		inv.setItem(0, InventoryHelper.back());
		inv.setItem(1, InventoryHelper.background());
		createShowItem(2, Material.WHEAT, farmer.getWheatPrice());
		createShowItem(3, Material.CARROT, farmer.getCarrotPrice());
		createShowItem(4, Material.POTATO, farmer.getPotatoPrice());
		createShowItem(5, Material.BAKED_POTATO, farmer.getBakedPotatoPrice());
		createShowItem(6, Material.NETHER_WART, farmer.getNetherWartPrice());
		createShowItem(7, Material.PUMPKIN, farmer.getPumpkinPrice());
		createShowItem(8, Material.MELON_SLICE, farmer.getMelonSlicePrice());
	}

	private void createShowItem(int slot, Material material, int price) {
		ItemStack item = ItemBuilder.of(plugin, material)
				.lore(
						ColorUtils.chat("&7클릭하여 판매하세요!"),
						ColorUtils.chat(String.format("&7가격: &6%,d$", price))
				)
				.hideAllFlags()
				.build();
		this.inv.setItem(slot, item);
		setRecord(slot, new ShopRecord(material, price));
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inv;
	}
}
