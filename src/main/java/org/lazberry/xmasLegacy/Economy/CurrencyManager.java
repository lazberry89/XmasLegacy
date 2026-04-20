package org.lazberry.xmasLegacy.Economy;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmasLegacy.Settings.Constants;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;
import org.lazberry.xmasLegacy.XmasLegacy;

public class CurrencyManager {
    private final EconomyManager EM;
    private final XmasLegacy plugin;

    public CurrencyManager(EconomyManager EM, XmasLegacy plugin) {
        this.EM = EM;
        this.plugin = plugin;
    }

    public ItemStack currency(int amount) {
        if (amount < Constants.CURRENCY_MINIMUM) return null;
        return ItemBuilder.of(plugin, Constants.CURRENCY_ITEM)
                .setName(ColorUtils.chat("&6&l현금 수표: " + amount + "$"))
                .setLore(ColorUtils.chat("&7&l현금으로 사용 가능하며, 우클릭시 다시 입금됩니다.(수수료 없음)"))
                .setGlint(true)
                .setTag("money", String.valueOf(amount))
                .build();
    }

    public void currencyToBank(Player p, ItemStack money) {
        if (money == null || money.getType() == Material.AIR) return;

        String value = money.getPersistentDataContainer().get(
                plugin.getNamespacedKey("money"),
                PersistentDataType.STRING
        );

        if (value == null) return;

        try {
            int amount = Integer.parseInt(value);
            if (amount <= 0) return;

            int currentAmount = money.getAmount();
            if (currentAmount > 1) {
                money.setAmount(currentAmount - 1);
            } else {
                money.setAmount(0);
            }
            EM.deposit(p, amount);

        } catch (NumberFormatException e) {
            plugin.getLogger().warning("잘못된 수표 데이터 발견: " + p.getName());
            plugin.playConsoleSound();
        }
    }
}
