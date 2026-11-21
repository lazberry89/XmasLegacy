package org.lazberry.xmaslegacy.role.shop.role.farmer;

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
        inv = Bukkit.createInventory(this, 9, ColorUtils.chat("상점선택"));
        var bg = InventoryHelper.background();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, bg);
        }
		inv.setItem(2, button(plugin, Material.IRON_HOE, "&6&l도구상점",
				"&7물뿌리개, 스프링클러, 비료, 화분 같은", "&7농사 도구를 구매합니다."));
		inv.setItem(4, button(plugin, Material.WHEAT_SEEDS, "&6&l씨앗상점",
				"&7새로운 작물의 씨앗을 구매하여", "&7농사를 시작해보세요!"));
		inv.setItem(6, button(plugin, Material.GOLDEN_CARROT, "&6&l작물판매",
				"&7수확한 작물을 판매합니다.", "&7별이 많을수록 비싸게 팔립니다."));
    }

    private ItemStack button(XmasLegacy plugin, Material icon, String name, String... lore) {
        return ItemBuilder.of(plugin, icon)
                .name(ColorUtils.chat(name))
                .lore(java.util.Arrays.stream(lore).map(ColorUtils::chat).toList())
                .hideAllFlags()
                .build();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
