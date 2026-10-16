package org.lazberry.xmaslegacy.role.shop.role.hunter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.role.shop.role.RoleShop;
import org.lazberry.xmaslegacy.role.shop.role.RoleShopManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.Objects;

public class HunterSellingShop extends RoleShop implements InventoryHolder {
    private final XmasLegacy plugin;
    private final Inventory inv;

    public HunterSellingShop(XmasLegacy plugin, RoleShopManager.Hunter hunter) {
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(this, 36, ColorUtils.chat("전리품 판매"));

        var bg = InventoryHelper.background();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, bg);
        }
        //00 01 02 03 04 05 06 07 08
        //09 10 11 12 13 14 15 16 17
        //18 19 20 21 22 23 24 25 26
        this.inv.setItem(0, InventoryHelper.back());
        createRecordAndSet(10, Material.ROTTEN_FLESH, hunter.getRottenFleshPrice());
        createRecordAndSet(11, Material.BONE, hunter.getBonePrice());
        createRecordAndSet(12, Material.ARROW, hunter.getArrowPrice());
        createRecordAndSet(13, Material.STRING, hunter.getStringPrice());
        createRecordAndSet(14, Material.SPIDER_EYE, hunter.getSpiderEyePrice());
        createRecordAndSet(15, Material.GUNPOWDER, hunter.getGunpowderPrice());
        createRecordAndSet(16, Material.SLIME_BALL, hunter.getSlimeBallPrice());

        createRecordAndSet(19, Material.PHANTOM_MEMBRANE, hunter.getPhantomMembranePrice());
        createRecordAndSet(20, Material.ENDER_PEARL, hunter.getEnderPearlPrice());
        createRecordAndSet(21, Material.MAGMA_CREAM, hunter.getMagmaCreamPrice());
        createRecordAndSet(22, Material.WITHER_SKELETON_SKULL, hunter.getWitherSkeletonSkullPrice());
        createRecordAndSet(23, Material.BLAZE_ROD, hunter.getBlazeRodPrice());
        createRecordAndSet(24, Material.GHAST_TEAR, hunter.getGhastTearPrice());
        createRecordAndSet(25, Material.PRISMARINE_SHARD, hunter.getPrismarineShardPrice());
    }

    public boolean hasItem(int slot) {
        return !Objects.equals(inv.getItem(slot), null);
    }

    private void createRecordAndSet(int slot, Material material, int price) {
        ItemStack item = ItemBuilder.of(plugin, material)
                .lore(
                        ColorUtils.chat("&7클릭하여 판매하세요!"),
                        ColorUtils.chat(String.format("&7가격: &6%,d$", price))
                )
                .hideAllFlags()
                .build();
        this.inv.setItem(slot, item);
        setRecord(slot, new ShopRecord(material, price));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
