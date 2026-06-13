package xmasLegacy.Enchant;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;

public enum EnchantManager {
	INSTANCE;

    private final NamespacedKey key;

    private static final Component LEVEL_1 = ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆");
    private static final Component LEVEL_2 = ColorUtils.chat("&e★★☆☆☆☆☆&6☆☆&c☆");
    private static final Component LEVEL_3 = ColorUtils.chat("&e★★★☆☆☆☆&6☆☆&c☆");
    private static final Component LEVEL_4 = ColorUtils.chat("&e★★★★☆☆☆&6☆☆&c☆");
    private static final Component LEVEL_5 = ColorUtils.chat("&e★★★★★☆☆&6☆☆&c☆");
    private static final Component LEVEL_6 = ColorUtils.chat("&e★★★★★★☆&6☆☆&c☆");
    private static final Component LEVEL_7 = ColorUtils.chat("&e★★★★★★★&6☆☆&c☆");
    private static final Component LEVEL_8 = ColorUtils.chat("&e★★★★★★★&6★☆&c☆");
    private static final Component LEVEL_9 = ColorUtils.chat("&e★★★★★★★&6★★&c☆");
    private static final Component LEVEL_10 = ColorUtils.chat("&e★★★★★★★&6★★&c★");
    private static final Component PRISM = ColorUtils.chat("&#C822FF★&#B22CFC★&#9C36F8★&#853FF5★&#6F49F2★&#5953EE★&#435DEB★&#2C66E8★&#1670E4★&#007AE1★");
    private static final List<Component> LORE_LIST = List.of(
            LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5,
            LEVEL_6, LEVEL_7, LEVEL_8, LEVEL_9, LEVEL_10
    );

    EnchantManager() {
        this.key = XmasLegacy.getInstance().getNamespacedKey("enchant");
    }

    @Range(from = 1, to = 10)
    public Component getLore(int lvl) {
        return LORE_LIST.get(lvl - 1);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEnchantable(@NotNull ItemStack item) {
        return getEnchantLevel(item) != null;
    }

    public @Nullable Integer getEnchantLevel(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(key, PersistentDataType.INTEGER);
    }

    public void editTag(ItemStack item, int lvl) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, lvl);
        item.setItemMeta(meta);
    }

    private void applyLevelChange(ItemStack item, int newLvl) {
        editTag(item, newLvl);

        item.editMeta(meta -> {
            List<Component> lore = meta.lore();
            if (lore == null) lore = new ArrayList<>();

            Component starLore = getLore(newLvl);

            if (lore.isEmpty()) {
                lore.add(starLore);
            } else {
                lore.set(0, starLore);
            }
            meta.lore(lore);
        });
    }

    public ResultType enchant(@NotNull Inventory inv) {
        return enchant(inv.getItem(13));
    }

    /**
     * 대망의 확률 연산 강화 메서드
     */
    public ResultType enchant(@Nullable ItemStack item) {
        if (item == null) return ResultType.FAIL;
        if (!isEnchantable(item)) return ResultType.FAIL;

        Integer currentLvl = getEnchantLevel(item);
        if (currentLvl == null || currentLvl >= 10) return ResultType.FAIL;

        double rand = Math.random() * 100.0;
        int nextLvl = currentLvl;
        ResultType result = ResultType.FAIL;

        EnchantChance chance = getChanceInfo(currentLvl);

        if (rand < chance.success()) {
            nextLvl = currentLvl + 1;
            result = ResultType.SUCCEED;
        } else if (rand < chance.success() + chance.fail()) {
            if (currentLvl >= 8) nextLvl = currentLvl - 2;
            else if (currentLvl >= 4) nextLvl = currentLvl - 1;
        } else {
            result = ResultType.BREAK;
        }

        if (result != ResultType.BREAK && currentLvl != nextLvl) {
            applyLevelChange(item, nextLvl);
        }

        return result;
    }

    public record EnchantChance(double success, double fail, double breakChance) {}
    /**
     * 특정 레벨의 강화 확률 정보(성공, 실패, 파괴)를 반환합니다.
     * @param lvl 현재 아이템의 강화 레벨 (1 ~ 9)
     * @return 성공/실패/파괴 확률이 담긴 EnchantChance 객체 (유효하지 않은 레벨이면 모든 확률 0.0)
     */
    public @NotNull EnchantChance getChanceInfo(int lvl) {
        return switch (lvl) {
            case 1 -> new EnchantChance(100.0, 0.0, 0.0);
            case 2 -> new EnchantChance(85.0, 15.0, 0.0);
            case 3 -> new EnchantChance(70.0, 30.0, 0.0);
            case 4 -> new EnchantChance(55.0, 45.0, 0.0);
            case 5 -> new EnchantChance(40.0, 60.0, 0.0);
            case 6 -> new EnchantChance(30.0, 69.0, 1.0);
            case 7 -> new EnchantChance(20.0, 78.0, 2.0);
            case 8 -> new EnchantChance(12.0, 85.0, 3.0);
            case 9 -> new EnchantChance(5.0, 90.0, 5.0);
            default -> new EnchantChance(0.0, 0.0, 0.0);
        };
    }
}