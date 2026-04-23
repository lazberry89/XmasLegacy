package org.lazberry.xmasLegacy.Economy;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.lazberry.xmasLegacy.Settings.Constants;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;
import org.lazberry.xmasLegacy.XmasLegacy;

@SuppressWarnings("ClassCanBeRecord")
public class CurrencyManager {
    private final EconomyManager EM;
    private final XmasLegacy plugin;

    public CurrencyManager(EconomyManager EM, XmasLegacy plugin) {
        this.EM = EM;
        this.plugin = plugin;
    }

    public static ItemStack currency(int amount) {
        return ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Constants.CURRENCY_ITEM)
                .setName(ColorUtils.chat("&6&l" + amount + "$"))
                .setLore(ColorUtils.chat("&7&l현금으로 사용 가능하며, 우클릭시 다시 입금됩니다."))
                .setGlint(true)
                .setTag("money", 100)
                .setMaxStackSize(Constants.MAX_CURRENCY_STACK)
                .setAmount(amount)
                .clone();
    }

    public void currencyToBank(Player p, ItemStack money) {
        if (money == null || money.getType() == Material.AIR) return;

        Integer value = money.getPersistentDataContainer().get(
                plugin.getNamespacedKey("money"),
                PersistentDataType.INTEGER
        );
        int count = money.getAmount();

        if (value == null) return;
        if (value == 100) {
            EM.deposit(p, 100 * count);
        }
    }
}
