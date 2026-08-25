package org.lazberry.xmaslegacy.gacha;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

@ConsumableClass
public class GachaStockInterface implements InventoryHolder {
	private final @NotNull Inventory inv;
    private final @NotNull BundleType type;

    public GachaStockInterface(@NotNull XmasLegacy plugin, @NotNull BundleType type, @NotNull GachaManager gm) {
        this.type = type;
        this.inv = Bukkit.createInventory(this, 54, ColorUtils.chat(String.format("&c&l%s : %d(개) / %d(전체)" ,type.getKor() , gm.getAllSortedByChance(type).size(), gm.getAll().size())));
        ItemStack none = ItemBuilder.of(plugin, Material.BARRIER).setName(ColorUtils.chat("")).setLore(ColorUtils.chat("")).hideAllFlags().build();
        if (gm.getAllSortedByChance(type).isEmpty()) {
            this.inv.setItem(0, none);
        } else {
            for (Gacha gacha : gm.getAllSortedByChance(type)) {
                this.inv.addItem(gacha.getShowItem());
            }
        }
    }

    public @NotNull BundleType getBundleType() {
        return this.type;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
