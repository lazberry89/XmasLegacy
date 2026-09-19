package org.lazberry.xmaslegacy.casino.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

public class CasinoShop implements InventoryHolder {
    private final Inventory inv;
    private final XmasLegacy plugin;
    private final ItemStack[] plans;
    private final int[] prices;

    public CasinoShop(XmasLegacy plugin) {
        this.plugin = plugin;
        this.plans = new ItemStack[]{plan1(), plan2(), plan3(), plan4(), plan5(), plan6(), plan7()};
        this.prices = new int[]{13_000, 60_000, 115_000, 220_000, 520_000, 980_000, 2_300_000};
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&c&l카지노 상점"));
        this.inv.setItem(0, Casino.entranceTicket());
        this.inv.setItem(1, bg());
        for (int i = 0; i < plans.length; i++) {
            inv.setItem(i + 2, plans[i]);
        }
    }

    public int getPriceBySlot(int slot) {
        if (slot == 1 || slot < 0 || slot > 8) return 0;
        if (slot == 0) return 5200;

        return prices[slot - 2];
    }

    public ItemStack getItemBySlot(int slot) {
        if (slot < 2 || slot > 8) return null;
        var amount = plans[slot - 2].getAmount();

        return Casino.coin(amount);
    }

    private ItemStack createPlan(int amount, String original, String discounted) {
        String lore = original.equals(discounted)
                ? "&7가격: " + original + "원"
                : "&7가격: &m" + original + "원&r&7 " + discounted + "원";
        return ItemBuilder.of(plugin, Casino.coin(amount))
                .setLore(ColorUtils.chat(lore))
                .build();
    }

    private ItemStack plan1() { return createPlan(1, "13000", "13000"); }
    private ItemStack plan2() { return createPlan(5, "65000", "60000"); }
    private ItemStack plan3() { return createPlan(10, "130000", "115000"); }
    private ItemStack plan4() { return createPlan(20, "260000", "220000"); }
    private ItemStack plan5() { return createPlan(40, "520000", "420000"); }
    private ItemStack plan6() { return createPlan(64, "832000", "630000"); }
    private ItemStack plan7() { return createPlan(99, "1287000", "900000"); }

    private ItemStack bg() {
        return ItemBuilder.of(plugin, Material.GRAY_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat(""))
                .setLore(ColorUtils.chat(""))
                .hideAllFlags()
                .build();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
