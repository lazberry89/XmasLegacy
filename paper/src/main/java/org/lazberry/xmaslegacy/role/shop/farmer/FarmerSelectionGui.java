package org.lazberry.xmaslegacy.role.shop.farmer;

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

public class FarmerSelectionGui implements InventoryHolder {
    private final Inventory inv;

    // 0 1 2 3 4 5 6 7 8
    public FarmerSelectionGui(XmasLegacy plugin) {
        inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&7상점선택"));
        var bg = InventoryHelper.background();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, bg);
        }

    }

    private ItemStack createSellShopButton(XmasLegacy plugin) {
        return ItemBuilder.of(plugin, Material.WHEAT)
                .name(ColorUtils.chat("&6&l생산물 판매하기"))
                .lore(ColorUtils.chat("&7생산한 농산물을 판매해보세요!"))
                .hideAllFlags()
                .build();
    }

    private ItemStack createSeedShopButton(XmasLegacy plugin) {
        return ItemBuilder.of(plugin, Material.WHEAT_SEEDS)
                .name(ColorUtils.chat("&6&l씨앗 상점"))
                .lore(ColorUtils.chat("&7농사 시작이 어렵다면, 씨앗을 구매하여 쉽게 시작해보세요."))
                .hideAllFlags().build();
    }

    private ItemStack createBuyCropButton(XmasLegacy plugin) {
        return ItemBuilder.of(plugin, Material.GOLDEN_CARROT)
                .name(ColorUtils.chat("&6&l작물 구매하기"))
                .lore(ColorUtils.chat("&7필요한 작물을 구매하여 맛있는 저녁을 만들어보세요!"))
                .hideAllFlags().build();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
