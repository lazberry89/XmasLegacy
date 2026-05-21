package xmasLegacy.Gacha;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.XmasLegacy;

public class BundleTypeInterface implements InventoryHolder {
    private final Inventory inv;
    private final GachaManager gm;

    public BundleTypeInterface() {
        this.gm = GachaManager.getInstance();
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
