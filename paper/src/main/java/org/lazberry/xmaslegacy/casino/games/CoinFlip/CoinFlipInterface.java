package org.lazberry.xmaslegacy.casino.games.CoinFlip;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

/** Coin flip table: put coins in the middle slot, then pick heads (left) or tails (right). */
public class CoinFlipInterface implements InventoryHolder {
	public static final int HEADS_SLOT = 3;
	public static final int STAKE_SLOT = 4;
	public static final int TAILS_SLOT = 5;

	private final Inventory inv;
	private final @Getter String machineId;

	public CoinFlipInterface(XmasLegacy plugin, String machineId) {
		this.machineId = machineId;
		this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&c&l동전 던지기"));
		var bg = InventoryHelper.background();
		for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, bg);
		inv.setItem(HEADS_SLOT, button(plugin, Material.SUNFLOWER, "&e&l앞면"));
		inv.setItem(STAKE_SLOT, null);
		inv.setItem(TAILS_SLOT, button(plugin, Material.ENDER_EYE, "&b&l뒷면"));
	}

	private static ItemStack button(XmasLegacy plugin, Material icon, String name) {
		return ItemBuilder.of(plugin, icon)
				.setName(ColorUtils.chat(name))
				.setLore(ColorUtils.chat("&7가운데 칸에 코인을 올리고 클릭하세요."),
						ColorUtils.chat("&7맞히면 코인을 2배로 돌려받습니다."))
				.hideAllFlags()
				.build();
	}

	/** Coins in the stake slot; anything that is not a casino coin counts as 0. */
	public int getStake() {
		ItemStack item = inv.getItem(STAKE_SLOT);
		return Casino.isCoin(item) ? item.getAmount() : 0;
	}

	public ItemStack takeStakeSlot() {
		ItemStack item = inv.getItem(STAKE_SLOT);
		inv.clear(STAKE_SLOT);
		return item;
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inv;
	}
}
