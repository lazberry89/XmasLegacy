package org.lazberry.xmaslegacy.enchant.material.shop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class EnchantMaterialShop implements InventoryHolder {
    private final List<ItemStack> showItems = new ArrayList<>(7);
    private final Inventory inv;
    private int currentSlot;

    private record ShopPlan(int amount, int discountPercent) {
        public int originalPrice() { return amount * 100; }
        public int finalPrice() { return originalPrice() * (100 - discountPercent) / 100; }
    }

    private static final List<ShopPlan> PLANS = List.of(
            new ShopPlan(1, 0),
            new ShopPlan(5, 5),
            new ShopPlan(10, 10),
            new ShopPlan(25, 15),
            new ShopPlan(50, 20),
            new ShopPlan(75, 25),
            new ShopPlan(99, 30)
    );

    public int getAmountBySlot(int slot) {
        var value = PLANS.get(slot);
        return value == null ? 0 : value.amount();
    }

    public int getPriceBySlot(int slot) {
        var value = PLANS.get(slot);
        return value == null ? 0 : value.finalPrice();
    }

    public EnchantMaterialShop(XmasLegacy plugin) {
        showItems.addAll(List.of(makeSpecialShowItem(EnchantMaterial.InfoBook(), plugin),
                makeSpecialShowItem(EnchantMaterial.DowngradeProtector(), plugin),
                makeSpecialShowItem(EnchantMaterial.BreakProtector(), plugin)));
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&c&l강화재료 상점"));
        this.inv.setItem(0, showItems.getFirst());
        this.inv.setItem(1, InventoryHelper.background());
        for (int i = 0; i < PLANS.size(); i++) {
            this.inv.setItem(i + 2, makePrismPlanItem(EnchantMaterial.PrismFractal(), PLANS.get(i), plugin));
        }
    }

    private ItemStack makePrismPlanItem(ItemStack baseItem, ShopPlan plan, XmasLegacy plugin) {
        String priceLore = plan.discountPercent() > 0
                ? String.format("&7가격: &8&m%,d원&r &e➔ &a%,d원 &7(%d%% 할인)", plan.originalPrice(), plan.finalPrice(), plan.discountPercent())
                : String.format("&7가격: &a%,d원", plan.finalPrice());

        return ItemBuilder.of(plugin, baseItem)
                .setAmount(plan.amount())
                .lore(
                        ColorUtils.chat("&7수량: &f" + plan.amount() + "개"),
                        ColorUtils.chat(priceLore)
                )
                .build();
    }

    private ItemStack makeSpecialShowItem(ItemStack item, XmasLegacy plugin) {
        return ItemBuilder.of(plugin, item)
                .name(ColorUtils.chat("&#C822FF강&#A731FA화&#853FF5재&#644EF0료 &#216BE6상&#007AE1점"))
                .lore(ColorUtils.chat("&7특수 아이템 상점으로 이동합니다."))
                .setAmount(1)
                .build();
    }

    public ItemStack nextItem() {
        if (showItems.isEmpty()) return null;
        currentSlot = (currentSlot + 1) % showItems.size();
        return showItems.get(currentSlot);
    }

    public void circulateShowItem() {
        this.inv.setItem(0, nextItem());
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
