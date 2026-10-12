package org.lazberry.xmaslegacy.role.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

public class ShopSelectionGui implements InventoryHolder {
    private final XmasLegacy plugin;
    private final Inventory inv;

    public ShopSelectionGui(XmasLegacy plugin) {
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("상점 선택하기"));

        //0 1 2 3 4 5 6 7 8
        var bg = InventoryHelper.background();
        for (int i = 0; i < this.inv.getSize(); i++) {
            this.inv.setItem(i, bg);
        }
        this.inv.setItem(1, createShowItem("농부상점", Material.COPPER_HOE));
        this.inv.setItem(3, createShowItem("광부상점", Material.COPPER_PICKAXE));
        this.inv.setItem(5, createShowItem("전리품상점", Material.COPPER_SWORD));
        this.inv.setItem(7, createShowItem("어부상점", Material.FISHING_ROD));
    }

    private ItemStack createShowItem(String name, Material material) {
        return ItemBuilder.of(plugin, material)
                .name(ColorUtils.chat("&6&l" + name))
                .lore(
                        ColorUtils.chat("&7클릭하여 선택하세요."),
                        ColorUtils.chat("&7선택 후 상인이 스폰됩니다!")
                )
                .hideAllFlags()
                .setGlint(true)
                .build();

    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
