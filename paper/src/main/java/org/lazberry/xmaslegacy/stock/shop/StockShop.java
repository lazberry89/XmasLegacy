package org.lazberry.xmaslegacy.stock.shop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.List;
import java.util.Optional;

public class StockShop implements InventoryHolder {
    private final ItemStack[] stockBySlot = new ItemStack[54];
    private final Inventory inv;

    public StockShop(List<ItemStack> stocks) {
        this.inv = Bukkit.createInventory(this, 54, ColorUtils.chat("&6&l주식 상점"));
        if (stocks.isEmpty()) return;
        for (int i = 0; i < Math.min(stocks.size(), 54); i++) {
            var item = stocks.get(i);
            this.inv.setItem(i, item);
            this.stockBySlot[i] = item;
        }
    }

    public Optional<ItemStack> getItemBySlot(int slot) {
        if (slot < 0 || slot >= stockBySlot.length) {
            return Optional.empty();
        }
        return Optional.ofNullable(stockBySlot[slot]);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
