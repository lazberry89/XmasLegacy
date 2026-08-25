package org.lazberry.xmaslegacy.gacha;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

@ConsumableClass
public class BundleTypeInterface implements InventoryHolder {
    private final @NotNull Inventory inv;

	public BundleTypeInterface(@NotNull GachaManager gm) {
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&c&l확률형 번들 &f4&r&l / 9"));
        for (ItemStack bundle : gm.getBundles()) {
            this.inv.addItem(bundle);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
