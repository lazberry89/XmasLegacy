package org.lazberry.xmaslegacy.enchant.material.shop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

public class ProtectorShop implements InventoryHolder {
    private final int[] priceBySlot = {0, 1, 0, 0, 10000, 0, 0, 50000, 0};
    private final Inventory inv;

    public ProtectorShop() {
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&#C822FF강&#A731FA화&#853FF5재&#644EF0료 &#216BE6상&#007AE1점"));
        var bg = InventoryHelper.background();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, bg);
        }

        this.inv.setItem(1, showItemBuilder(EnchantMaterial.InfoBook(), 1));
        this.inv.setItem(4, showItemBuilder(EnchantMaterial.DowngradeProtector(), 10_000));
        this.inv.setItem(7, showItemBuilder(EnchantMaterial.BreakProtector(), 50_000));
    }

    public ItemStack showItemBuilder(ItemStack item, int price) {
        return ItemBuilder.of(XmasLegacy.getInstance(), item)
                .addLore(ColorUtils.chat(""))
                .addLore(ColorUtils.chat(String.format("&7가격: &6%,d$", price)))
                .build();
    }

    public int getPriceBySlot(int slot) {
        if (slot > 8 || slot < 0) return 0;
        return priceBySlot[slot];
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
