package org.lazberry.xmaslegacy.casino.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

public class CasinoShop implements InventoryHolder {
    private final Inventory inv;
    private final XmasLegacy plugin;
    private final ItemStack[] plans;

    public CasinoShop(XmasLegacy plugin) {
        this.plugin = plugin;
        this.plans = new ItemStack[]{plan1(), plan2(), plan3(), plan4(), plan5(), plan6(), plan7()};
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&c&l카지노 상점"));
        this.inv.setItem(0, Casino.entranceTicket());
        this.inv.setItem(1, bg());
        for (int i = 0; i < plans.length; i++) {
            inv.setItem(i + 2, plans[i]);
        }
    }

    @Range(from = 0, to = 250)
    public int getAmountBySlot(int slot) {
        if (slot < 2 || slot > 8) return 0;
        ItemStack item = plans[slot - 2];
        return item != null ? item.getAmount() : 0;
    }

    private ItemStack plan1() {
        return ItemBuilder.of(plugin, Casino.coin(1))
                .setLore(ColorUtils.chat("&7가격: 13000원"))
                .build();
    }

    private ItemStack plan2() {
        return ItemBuilder.of(plugin, Casino.coin(5))
                .setLore(ColorUtils.chat("&7가격: &m65000원&r&7 60000원"))
                .build();
    }

    private ItemStack plan3() {
        return ItemBuilder.of(plugin, Casino.coin(10))
                .setLore(ColorUtils.chat("&7가격: &m130000원&r&7 115000원"))
                .build();
    }

    private ItemStack plan4() {
        return ItemBuilder.of(plugin, Casino.coin(20))
                .setLore(ColorUtils.chat("&7가격: &m260000원&r&7 220000원"))
                .build();
    }

    private ItemStack plan5() {
        return ItemBuilder.of(plugin, Casino.coin(50))
                .setLore(ColorUtils.chat("&7가격: &m650000원&r&7 520000원"))
                .build();
    }

    private ItemStack plan6() {
        return ItemBuilder.of(plugin, Casino.coin(100))
                .setLore(ColorUtils.chat("&7가격: &m1300000원&r&7 980000원"))
                .build();
    }

    private ItemStack plan7() {
        return ItemBuilder.of(plugin, Casino.coin(250))
                .setLore(ColorUtils.chat("&7가격: &m3250000원&r&7 2300000원"))
                .build();
    }

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
