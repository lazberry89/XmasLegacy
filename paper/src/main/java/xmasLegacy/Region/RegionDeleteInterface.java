package xmasLegacy.Region;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class RegionDeleteInterface implements InventoryHolder {
    private final @NotNull Inventory inv;
    private final @NotNull Region region;

    public RegionDeleteInterface(@NotNull Region region) {
        @NotNull XmasLegacy plugin = XmasLegacy.getInstance();
        this.region = region;
        this.inv = Bukkit.createInventory(this, 27, ColorUtils.chat("&c&l구역 삭제"));
        var blue = ItemBuilder.of(plugin, Material.BLUE_WOOL)
                .setName(ColorUtils.chat("&9&l삭제하기"))
                .setLore(ColorUtils.chat("&7해당 구역을 삭제합니다."))
                .hideAllFlags()
                .build().clone();
        var red = ItemBuilder.of(plugin, Material.RED_WOOL)
                .setName(ColorUtils.chat("&c&l돌아가기"))
                .setLore(ColorUtils.chat("&7다시 지역 설정으로 돌아갑니다."))
                .hideAllFlags()
                .build().clone();
        var bg = ItemBuilder.of(plugin, Material.GRAY_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat("")).setLore(ColorUtils.chat(""))
                .hideAllFlags().build().clone();
        for (int i = 0; i < this.inv.getSize(); i++) this.inv.setItem(i, bg);
        this.inv.setItem(11, blue);
        this.inv.setItem(15, red);
    }

    public @NotNull Region getRegion() {
        return this.region;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
