package org.lazberry.xmaslegacy.role.shop.farmer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.role.shop.RoleShopManager;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.HashSet;
import java.util.Set;

public class CropSellingShop implements InventoryHolder {
	private final Set<Material> sellItems = new HashSet<>();
	private final RoleShopManager.Farmer farmer;
	private final XmasLegacy plugin;
	private final Inventory inv;

	public CropSellingShop(XmasLegacy plugin, RoleShopManager.Farmer farmer) {
		this.farmer = farmer;
		this.plugin = plugin;
		this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("작물판매"));

		inv.setItem(0, InventoryHelper.back());
		inv.setItem(1, InventoryHelper.background());
		inv.setItem(2, createShowItem(Material.WHEAT, farmer.getWheatPrice()));
		inv.setItem(3, createShowItem(Material.CARROT, farmer.getCarrotPrice()));
		inv.setItem(4, createShowItem(Material.POTATO, farmer.getPotatoPrice()));
		inv.setItem(5, createShowItem(Material.BAKED_POTATO, farmer.getBakedPotatoPrice()));
		inv.setItem(6, createShowItem(Material.NETHER_WART, farmer.getNetherWartPrice()));
		inv.setItem(7, createShowItem(Material.PUMPKIN, farmer.getPumpkinPrice()));
		inv.setItem(8, createShowItem(Material.MELON_SLICE, farmer.getMelonSlicePrice()));
	}

	private int getPriceBySlot(int slot) {
		return switch (slot) {
			case 2 -> farmer.getWheatPrice();
			case 3 -> farmer.getCarrotPrice();
			case 4 -> farmer.getPotatoPrice();
			case 5 -> farmer.getBakedPotatoPrice();
			case 6 -> farmer.getNetherWartPrice();
			case 7 -> farmer.getPumpkinPrice();
			case 8 -> farmer.getMelonSlicePrice();
			default -> 0;
		};
	}

	private boolean isSellItem(Material material) {
		return sellItems.contains(material);
	}

	private ItemStack createShowItem(Material material, int price) {
		sellItems.add(material);
		return ItemBuilder.of(plugin, material)
				.lore(
						ColorUtils.chat("&7클릭하여 판매하세요."),
						ColorUtils.chat("&7가격: &6" + price + "$")
				)
				.hideAllFlags()
				.build();
	}

	public void tryPurchase(Player p, @Nullable User user, int slot, int amount, boolean shift) {
		ItemStack item = inv.getItem(slot);
		if (item == null  || user == null || amount == 0) {
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			return;
		}

		Material type = item.getType();
		if (!isSellItem(type)) {
			p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			return;
		}

		if (shift) amount = InventoryHelper.getItemCount(p, type);
		if (amount == 0) return;

		if (InventoryHelper.removeItem(p, type, amount)) {
			int price = getPriceBySlot(slot);
			if (price == 0) {
				p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
				return;
			}
			user.addDollars(price * amount);
			p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
		} else p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inv;
	}
}
