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

    public GachaStockInterface(XmasLegacy plugin) {
        this.plugin = plugin;
        this.GM = plugin.GM;
        this.inv = Bukkit.createInventory(this, 54, ColorUtils.chat(String.format("&c&l확률형 아이템 : %d(개)", GM.getAll().size())));
        ItemStack none = ItemBuilder.of(plugin, Material.BARRIER).setName(ColorUtils.chat("")).setLore(ColorUtils.chat("")).hideAllFlags().build();
        if (GM.getAll().isEmpty()) {
            this.inv.setItem(0, none);
        } else {
            for (Gacha gacha : GM.getAllSortedByChance()) {
                this.inv.addItem(gacha.getShowItem());
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
