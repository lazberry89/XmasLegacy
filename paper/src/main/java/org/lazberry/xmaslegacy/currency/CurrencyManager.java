package org.lazberry.xmaslegacy.currency;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.EconomyManager;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.UUID;

@Registry.Exclude(type = ServerType.LOBBY)
public class CurrencyManager {
	private final @NotNull EconomyManager em;

	@Inject
	public CurrencyManager(@NotNull EconomyManager em) {
		this.em = em;
	}

	public static @NotNull ItemStack currency(@NotNull XmasLegacy plugin) {
		return ItemBuilder.of(plugin, Material.GOLD_INGOT)
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

		if (value == 100)
			if (em.deposit(uuid, 100 * count)) money.setAmount(0);
	}
}
