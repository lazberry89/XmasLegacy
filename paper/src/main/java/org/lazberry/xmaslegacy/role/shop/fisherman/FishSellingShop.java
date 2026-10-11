package org.lazberry.xmaslegacy.role.shop.fisherman;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.role.shop.RoleShop;
import org.lazberry.xmaslegacy.role.shop.RoleShopManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

public class FishSellingShop extends RoleShop implements InventoryHolder {

	@Getter
	private final ShopRecord[] records = new ShopRecord[9];
	private final XmasLegacy plugin;
	private final Inventory inv;

	public FishSellingShop(XmasLegacy plugin, RoleShopManager.Fisherman fisherman) {
		this.plugin = plugin;
		this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("어부상점"));
		this.inv.setItem(0, InventoryHelper.back());
		this.inv.setItem(1, InventoryHelper.background());
		createRecordAndSet(2, Material.COD, fisherman.getCodPrice());
		createRecordAndSet(3, Material.COOKED_COD, fisherman.getCookedCodPrice());
		createRecordAndSet(4, Material.SALMON, fisherman.getSalmonPrice());
		createRecordAndSet(5, Material.COOKED_SALMON, fisherman.getCookedSalmonPrice());
		createRecordAndSet(6, Material.TROPICAL_FISH, fisherman.getTropicalFishPrice());
		createRecordAndSet(7, Material.PUFFERFISH, fisherman.getPufferfishPrice());
		createRecordAndSet(8, Material.NAUTILUS_SHELL, fisherman.getNautilusShellPrice());
	}

	private void createRecordAndSet(int slot, Material material, int price) {
		ItemStack item = ItemBuilder.of(plugin, material)
				.lore(
						ColorUtils.chat("&7클릭하여 판매하세요!"),
						ColorUtils.chat("&7가격: &6" + price + "$")
				)
				.hideAllFlags()
				.build();
		this.inv.setItem(slot, item);
		this.records[slot] = new ShopRecord(material, price);
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inv;
	}
}
