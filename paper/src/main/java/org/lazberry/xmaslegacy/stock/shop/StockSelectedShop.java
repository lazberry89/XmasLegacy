package org.lazberry.xmaslegacy.stock.shop;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.stock.Stock;
import org.lazberry.xmaslegacy.stock.StockItemBuilder;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

public class StockSelectedShop implements InventoryHolder {
    private final Inventory inv;
    private final StockItemBuilder builder;
    private final @Getter Stock stock;
    private final XmasLegacy plugin;
    private int amount = 1;

    public StockSelectedShop(Stock selected, StockItemBuilder builder, XmasLegacy plugin) {
        this.builder = builder;
        this.stock = selected;
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&6&l선택됨: " + selected.getName()));
        var bg = background();
        for (int i = 0; i < inv.getSize(); i++) {
            this.inv.setItem(i, bg);
        }
        update();
    }

    public void update() {
        ItemStack show = builder.createStockShowItem(stock);
        show.setAmount(amount);

        this.inv.setItem(0, back());
        this.inv.setItem(3, less());
        this.inv.setItem(4, show);
        this.inv.setItem(5, more());
        this.inv.setItem(8, purchase());
    }

    public void addAmount() {
        this.amount = Math.min(64, amount + 1);
        update();
    }

    public void reduceAmount() {
        this.amount = Math.max(1, amount - 1);
        update();
    }

    public void setAmount(int amount) {
        this.amount = Math.max(1, amount);
        update();
    }

    private ItemStack purchase() {
        return ItemBuilder.of(plugin, Material.BELL)
                .setName(ColorUtils.chat("&6&l구매하기"))
                .setLore(ColorUtils.chat("&7현재수량: " + amount + "주"),
                        ColorUtils.chat("&7총 가격: " + (amount * stock.getCurrentPrice())))
                .build();
    }

    private ItemStack back() {
        return ItemBuilder.of(plugin, Material.BARRIER)
                .setName(ColorUtils.chat("&4&l돌아가기"))
                .setLore(ColorUtils.chat("&7버튼을 클릭해 주식창으로 돌아갑니다."))
                .build();
    }

    private ItemStack less() {
        return ItemBuilder.of(plugin, Material.BLUE_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat("&9&l수량 낮추기"))
                .setLore(ColorUtils.chat("&7현재수량: " + amount + "주"))
                .build();
    }

    private ItemStack more() {
        return ItemBuilder.of(plugin, Material.RED_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat("&c&l수량 올리기"))
                .setLore(ColorUtils.chat("&7현재수량: " + amount + "주"))
                .build();
    }

    private ItemStack background() {
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
