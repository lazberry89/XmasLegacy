package org.lazberry.xmaslegacy.Economy.Currency;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.EconomyManager;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerManager;

import java.util.UUID;

@Registry
public enum CurrencyManager implements ServerManager {
	INSTANCE;

	public @NotNull ItemStack currency() {
		return ItemBuilder.of(XmasLegacy.getInstance(), Material.GOLD_INGOT)
				.setName(ColorUtils.chat("&6&l100$"))
				.setLore(ColorUtils.chat("&7&l현금으로 사용 가능하며, 우클릭시 다시 입금됩니다."))
				.setGlint(true)
				.setTag("money", 100)
				.setMaxStackSize(Constants.MAX_CURRENCY_STACK)
				.build().clone();
	}

	public void currencyToBank(@NotNull UUID uuid, @NotNull ItemStack money) {
		var key = KeyUtils.get("money");
		int value = KeyUtils.get(money, key, 0);
		int count = money.getAmount();

		if (value == 100) EconomyManager.INSTANCE.deposit(uuid, 100 * count);
	}

	@Override
	public void init() {

	}
}
