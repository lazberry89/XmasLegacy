package xmasLegacy.Gacha;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

@SuppressWarnings("FieldCanBeLocal, unused")
public class GachaStockInterface implements InventoryHolder {
    private final XmasLegacy plugin;
    private final GachaManager GM;
    private final Inventory inv;
    private final BundleType type;

    public GachaStockInterface(XmasLegacy plugin, @NotNull BundleType type) {
        this.plugin = plugin;
        this.type = type;
        this.GM = plugin.GM;
        this.inv = Bukkit.createInventory(this, 54, ColorUtils.chat(String.format("&c&l%s : %d(개) / %d(전체)" ,type.getKor() ,GM.getAllSortedByChance(type).size(), GM.getAll().size())));
        ItemStack none = ItemBuilder.of(plugin, Material.BARRIER).setName(ColorUtils.chat("")).setLore(ColorUtils.chat("")).hideAllFlags().build();
        if (GM.getAllSortedByChance(type).isEmpty()) {
            this.inv.setItem(0, none);
        } else {
            for (Gacha gacha : GM.getAllSortedByChance(type)) {
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
