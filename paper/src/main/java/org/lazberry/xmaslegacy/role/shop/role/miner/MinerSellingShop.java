package org.lazberry.xmaslegacy.role.shop.role.miner;

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

public class MinerSellingShop extends RoleShop implements InventoryHolder {
	private final Inventory inv;
	private final XmasLegacy plugin;

	public MinerSellingShop(XmasLegacy plugin, RoleShopManager.Miner miner) {
		this.plugin = plugin;
		this.inv = Bukkit.createInventory(this, 36, ColorUtils.chat("광부상점"));

		var bg = InventoryHelper.background();
		for (int i = 0; i < inv.getSize(); i++) {
			inv.setItem(i, bg);
		}
		//10 ~ 16
		//19 ~ 25
		for (int i = 10; i < 17; i++) {
			inv.setItem(i, null);
		}
		for (int i = 19; i < 26; i++) {
			inv.setItem(i, null);
		}
		this.inv.setItem(0, InventoryHelper.back());
		createRecordAndSet(10, Material.COBBLESTONE, miner.getCobbleStonePrice());
		createRecordAndSet(11, Material.COBBLED_DEEPSLATE, miner.getCobbledDeepslatePrice());
		createRecordAndSet(12, Material.COAL, miner.getCoalPrice());
		createRecordAndSet(13, Material.REDSTONE, miner.getRedstonePrice());
		createRecordAndSet(14, Material.LAPIS_LAZULI, miner.getLapisPrice());
		createRecordAndSet(15, Material.QUARTZ, miner.getQuartzPrice());
		createRecordAndSet(16, Material.RAW_IRON, miner.getRawIronPrice());
		createRecordAndSet(19, Material.IRON_INGOT, miner.getIronPrice());
		createRecordAndSet(20, Material.RAW_GOLD, miner.getRawGoldPrice());
		createRecordAndSet(21, Material.GOLD_INGOT, miner.getGoldPrice());
		createRecordAndSet(22, Material.DIAMOND, miner.getDiamondPrice());
		createRecordAndSet(23, Material.EMERALD, miner.getEmeraldPrice());
		createRecordAndSet(24, Material.NETHERITE_SCRAP, miner.getNetheriteScrapPrice());
		createRecordAndSet(25, Material.NETHERITE_INGOT, miner.getNetheriteIngotPrice());
	}

	private void createRecordAndSet(int slot, Material material, int price) {
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
