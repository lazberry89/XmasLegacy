package xmaslegacy.Economy.Currency;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.EconomyManager;
import xmaslegacy.Utils.ItemBuilder;
import xmaslegacy.Utils.KeyUtils;
import xmaslegacy.XmasLegacy;

import java.util.UUID;

public record CurrencyManager(@NotNull EconomyManager em, @NotNull XmasLegacy plugin) {

	public static @NotNull ItemStack currency(int amount) {
		return ItemBuilder.of(XmasLegacy.getInstance(), Material.GOLD_INGOT)
				.setName(ColorUtils.chat("&6&l" + amount + "$"))
				.setLore(ColorUtils.chat("&7&l현금으로 사용 가능하며, 우클릭시 다시 입금됩니다."))
				.setGlint(true)
				.setTag("money", 100)
				.setMaxStackSize(Constants.MAX_CURRENCY_STACK)
				.setAmount(amount)
				.clone();
	}

	public void currencyToBank(UUID uuid, ItemStack money) {
		if (money == null || money.getType() == Material.AIR) return;

		Integer value = money.getPersistentDataContainer().get(
				KeyUtils.get("money"),
				PersistentDataType.INTEGER
		);
		int count = money.getAmount();

		if (value == null) return;
		if (value == 100) em.deposit(uuid, 100 * count);
	}
}
