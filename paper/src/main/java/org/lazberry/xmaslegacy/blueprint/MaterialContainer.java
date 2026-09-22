package org.lazberry.xmaslegacy.blueprint;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryComponents;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

@Getter
public class MaterialContainer implements InventoryHolder {
    private final Inventory inv;
    private final BluePrint bluePrint;
    private final XmasLegacy plugin;
    private Material currentItem;
    private int currentIndex = -1;

    public MaterialContainer(BluePrint bluePrint, XmasLegacy plugin) {
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&6&l재료함"));
        this.bluePrint = bluePrint;
        this.plugin = plugin;
        var bg = InventoryComponents.background();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, bg);
        }
        inv.setItem(2, giveBack());
        inv.setItem(3, save());
        inv.setItem(4, null);
        inv.setItem(5, showItem());
    }

    public void giveBackSavedItem(Player p, Material material) {
        if (bluePrint.isBuildingMaterial(material)) {
            InfoUtils.error(p, "돌려받을 수 없는 재료입니다.");
            return;
        }
        int amount = bluePrint.getRemainingAmount(material);
        if (amount <= 0) {
            InfoUtils.error(p, "남아있는 재료가 없습니다.");
            return;
        }
        bluePrint.addBuildingMaterial(material, bluePrint.getRemainingAmount(material));

        var giveBack = new ItemStack(material);
        giveBack.setAmount(amount);

        p.getInventory().addItem(giveBack);
    }

    private ItemStack save() {
        return ItemBuilder.of(plugin, Material.GREEN_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat("&a&l저장하기"))
                .setLore(
                        ColorUtils.chat("&7클릭하여 도면 사용에 필요한 재료를 저장합니다."),
                        ColorUtils.chat("&7언제든지 다시 재료를 꺼낼 수 있습니다."))
                .hideAllFlags()
                .build();
    }

    private ItemStack giveBack() {
        return ItemBuilder.of(plugin, Material.RED_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat("&c&l돌려받기"))
                .setLore(ColorUtils.chat("&7저장했던 아이템을 모두 돌려받습니다."))
                .hideAllFlags()
                .build();
    }

    public boolean saveMaterial(ItemStack item) {
        if (item == null || item.getType().isAir()
                || item.getAmount() == 0) return false;
        int amount = item.getAmount();
        Material type = item.getType();

        if (currentItem != type) return false;
        int remaining = bluePrint.addBuildingMaterial(type, amount);
        if (remaining > 0) {
            var giveBack = item.clone();
            giveBack.setAmount(remaining);
            inv.setItem(4, item.clone());
        }
        return true;
    }

    private ItemStack createShowItem(Material material, int maxAmount, int currentProcess) {
        var key = material.getItemTranslationKey();
        Component nameComponent = key != null ? Component.translatable(key) : ColorUtils.chat(material.name());

        boolean isFilled = currentProcess >= maxAmount;
        NamedTextColor color = isFilled ? NamedTextColor.GREEN : NamedTextColor.RED;
        Component lore = ColorUtils.chat("&7진행도: " + currentProcess + "/" + maxAmount);
        if (isFilled) lore.append(ColorUtils.chat(" &7[완료됨]"));

        return ItemBuilder.of(plugin, material)
                .setName(nameComponent.color(color))
                .setLore(lore)
                .hideAllFlags()
                .setGlint(isFilled)
                .build();
    }

    public void next() {
        var materials = bluePrint.getOrderedMaterialList();
        if (materials.isEmpty()) return;

        currentIndex = (currentIndex + 1) % materials.size();
        this.currentItem = materials.get(currentIndex);

        int maxAmount = bluePrint.getNeededAmount(currentItem);
        int currentProcess = Math.max(0, bluePrint.getRemainingAmount(currentItem));

        inv.setItem(5, createShowItem(currentItem, maxAmount, currentProcess));
    }

    private ItemStack showItem() {
        return ItemBuilder.of(plugin, Material.CHEST)
                .setName(ColorUtils.chat("&6&l필요한 재료: "))
                .setLore(ColorUtils.chat("&7아이콘을 클릭하면 차례대로 필요한 재료들을 확인할 수 있습니다!"))
                .hideAllFlags()
                .build();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
