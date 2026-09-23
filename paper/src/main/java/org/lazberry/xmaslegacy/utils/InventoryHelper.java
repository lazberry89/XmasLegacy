package org.lazberry.xmaslegacy.utils;

import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.bags.BagManager;

import java.util.Arrays;
import java.util.Objects;

public class InventoryHelper {
    private static @Setter BagManager bm;

    public static ItemStack background() {
        return ItemBuilder.of(XmasLegacy.getInstance(), Material.GRAY_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat(""))
                .setLore(ColorUtils.chat(""))
                .hideAllFlags()
                .build();
    }

    public static void giveItemOrDrop(@Nullable Player p, ItemStack... items) {
        if (items.length == 0 || p == null || !p.isOnline()) return;

        var loc = p.getLocation();
        var world = loc.getWorld();
        final var remains = p.getInventory().addItem(items);

        var itemList = remains.values();
        if (itemList.isEmpty()) return;

        itemList.stream()
                .filter(Objects::nonNull)
                .forEach(i -> world.dropItemNaturally(loc, i));
    }

    public static void giveItemOrKeep(@Nullable Player p, ItemStack... items) {
        if (items.length == 0 || p == null || !p.isOnline()) return;

        final var remains = p.getInventory().addItem(items);
        var remainItems = remains.values();
        if (remainItems.isEmpty()) return;

        bm.addAll(p, remainItems);
    }
}
