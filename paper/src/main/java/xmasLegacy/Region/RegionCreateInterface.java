package xmasLegacy.Region;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class RegionCreateInterface implements InventoryHolder {
    private final @NotNull XmasLegacy plugin;
    private final @NotNull Inventory inv;

    public RegionCreateInterface() {
        this.plugin = XmasLegacy.getInstance();
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&a&l구역 생성"));
        var green = ItemBuilder.of(plugin, Material.GREEN_STAINED_GLASS_PANE).setName(ColorUtils.chat("")).setLore(ColorUtils.chat("")).hideAllFlags().build().clone();
        var red = ItemBuilder.of(plugin, Material.RED_STAINED_GLASS_PANE).setName(ColorUtils.chat("")).setLore(ColorUtils.chat("")).hideAllFlags().build().clone();
        for (int i = 0; i < 4; i++) this.inv.setItem(i, green);
        for (int i = 5; i < 9; i++) this.inv.setItem(i, red);
        this.inv.setItem(4, ButtonMaker());
    }

    private @NotNull ItemStack ButtonMaker() {
        return ItemBuilder.of(plugin, Material.BRICKS)
                .setName(ColorUtils.chat("&6&l구역 생성하기"))
                .hideAllFlags()
                .setLore(ColorUtils.chat("&7클릭하여 구역을 생성하세요."))
                .build().clone();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
