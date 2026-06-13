package xmasLegacy.Region.Gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.Region.Region;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class RegionSettingInterface implements InventoryHolder {
    private final @NotNull XmasLegacy plugin;
    private final @NotNull Region region;
    private final @NotNull Inventory inv;

    public RegionSettingInterface(@NotNull Region region) {
        this.region = region;
        this.plugin = XmasLegacy.getInstance();
        OfflinePlayer owner = Bukkit.getOfflinePlayer(region.getOwner());
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&6&l" + owner.getName()));
        for (int i = 0; i < this.inv.getSize(); i++) this.inv.setItem(i, maker(region, i));
    }

    @CheckReturnValue
    private @NotNull ItemStack maker(Region region, int slot) {
        return switch (slot) {
            case 0 -> ItemBuilder.of(plugin, Material.BEACON).setName(ColorUtils.chat("&b&l" + region.Id())).setLore(ColorUtils.chat("&7구역이 정상적으로 작동중입니다.")).build().clone();
            case 1, 7 -> ItemBuilder.of(plugin, Material.GRAY_STAINED_GLASS_PANE).setName(ColorUtils.chat("")).hideAllFlags().build().clone();
            case 2 -> ItemBuilder.of(plugin, Material.IRON_BOOTS).setName(ColorUtils.chat("&6&l입장 권한"))
                    .setLore(
                            region.isEntryAllowed() ? ColorUtils.chat("&9&l▶ 허용") : ColorUtils.chat("&7▶ 허용"),
                            region.isEntryAllowed() ? ColorUtils.chat("&7▶ 차단") : ColorUtils.chat("&c&l▶ 차단")
                    )
                    .hideAllFlags()
                    .setGlint(region.isEntryAllowed())
                    .build().clone();
            case 3 -> ItemBuilder.of(plugin, Material.IRON_PICKAXE).setName(ColorUtils.chat("&6&l상호작용 권한"))
                    .setLore(
                            region.isInteractionAllowed() ? ColorUtils.chat("&9&l▶ 허용") : ColorUtils.chat("&7▶ 허용"),
                            region.isInteractionAllowed() ? ColorUtils.chat("&7▶ 차단") : ColorUtils.chat("&c&l▶ 차단")
                    )
                    .hideAllFlags()
                    .setGlint(region.isInteractionAllowed())
                    .build().clone();
            case 8 -> ItemBuilder.of(plugin, Material.BARRIER).setName(ColorUtils.chat("&4&l구역 삭제하기"))
                    .setLore(ColorUtils.chat("&7클릭시 재확인 후 구역을 삭제합니다."), ColorUtils.chat("&7별도의 배상금 혹은 보상은 존재하지 않습니다"))
                    .hideAllFlags().build().clone();
            default -> new ItemStack(Material.AIR);
        };
    }

    public @NotNull Region getRegion() {
        return this.region;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
