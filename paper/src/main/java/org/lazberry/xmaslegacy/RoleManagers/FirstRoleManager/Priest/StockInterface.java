package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Priest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;

public class StockInterface implements InventoryHolder {
    private final Inventory inv;

    public StockInterface() {
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&c&l상점 창고"));
        ItemStack bg = ItemBuilder.of(XmasLegacy.getPlugin(XmasLegacy.class), Material.GRAY_STAINED_GLASS_PANE).setName(ColorUtils.chat("")).setLore(ColorUtils.chat("")).hideAllFlags().build();
        ItemStack check = ItemBuilder.of(XmasLegacy.getPlugin(XmasLegacy.class), Material.GREEN_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat("&a&l게시하기"))
                .setLore(ColorUtils.chat("&7판매할 물건을 올리고 클릭하여 상점에 게시하세요!"))
                .hideAllFlags()
                .build();
        ItemStack giveBack = ItemBuilder.of(XmasLegacy.getPlugin(XmasLegacy.class), Material.RED_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat("&c&l판매품 돌려받기"))
                .setLore(ColorUtils.chat("&7상점에 등록한 아이템을 다시 돌려받습니다."))
                .hideAllFlags()
                .build();
        ItemStack sell = ItemBuilder.of(XmasLegacy.getPlugin(XmasLegacy.class), Material.BLUE_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat("&9&l상점 시작하기"))
                .setLore(ColorUtils.chat("&7등록한 아이템으로 상점을 시작하세요!"))
                .hideAllFlags()
                .build();
        for (int i = 0; i < this.inv.getSize(); i++) {
            this.inv.setItem(i, bg);
        }
        this.inv.setItem(4, new ItemStack(Material.AIR));
        this.inv.setItem(3, check);
        this.inv.setItem(5, giveBack);
        this.inv.setItem(6, sell);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
