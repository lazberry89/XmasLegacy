package org.lazberry.xmaslegacy.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.XmasLegacy;

public class InventoryComponents {

    public static ItemStack background() {
        return ItemBuilder.of(XmasLegacy.getInstance(), Material.GRAY_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat(""))
                .setLore(ColorUtils.chat(""))
                .hideAllFlags()
                .build();
    }
}
