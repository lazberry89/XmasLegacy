package org.lazberry.xmaslegacy.enchant;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.ArrayList;
import java.util.List;

@Registry.Exclude(type = ServerType.LOBBY)
public class EnchantManager implements Initiator {
    private final NamespacedKey key;
	private final NamespacedKey damageModifierKey;

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

    public EnchantManager() {
	    this.key = KeyUtils.get("enchant");
	    this.damageModifierKey = KeyUtils.get("enchant_damage");
    }

	public boolean isEnchantableMaterial(Material material) {
		String name = material.name().toLowerCase();
		return name.contains("pickaxe") || name.contains("sword") || name.contains("axe")
				|| name.contains("shovel");
	}

	public boolean setEnchantable(ItemStack item) {
		if (item == null) return false;
		if (!isEnchantableMaterial(item.getType())) return false;
		KeyUtils.set(item, key, 1);
		item.editMeta(meta -> {
			meta.setRarity(ItemRarity.EPIC);
			meta.setEnchantmentGlintOverride(true);
			List<Component> lore = new ArrayList<>(List.of(LEVEL_1, ColorUtils.chat("&7강화가능")));
			meta.lore(lore);
		});
		updateItemBuffs(item, 1);
		return true;
	}

    @Range(from = 1, to = 10)
    public @NotNull Component getLore(int lvl) {
        return LORE_LIST.get(Math.clamp(lvl - 1, 0, 9));
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

    public void editTag(@NotNull ItemStack item, int lvl) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, lvl);
        item.setItemMeta(meta);
    }

	private double getBaseDamage(Material material) {
		String name = material.name();
		if (name.endsWith("_SWORD")) {
			if (name.startsWith("WOODEN") || name.startsWith("GOLDEN")) return 4.0;
			if (name.startsWith("STONE")) return 5.0;
			if (name.startsWith("IRON")) return 6.0;
			if (name.startsWith("DIAMOND")) return 7.0;
			if (name.startsWith("NETHERITE")) return 8.0;
		} else if (name.endsWith("_AXE")) {
			if (name.startsWith("WOODEN") || name.startsWith("GOLDEN")) return 7.0;
			if (name.startsWith("STONE") || name.startsWith("IRON") || name.startsWith("DIAMOND")) return 9.0;
			if (name.startsWith("NETHERITE")) return 10.0;
		} else if (name.endsWith("_PICKAXE")) {
			if (name.startsWith("WOODEN") || name.startsWith("GOLDEN")) return 2.0;
			if (name.startsWith("STONE")) return 3.0;
			if (name.startsWith("IRON")) return 4.0;
			if (name.startsWith("DIAMOND")) return 5.0;
			if (name.startsWith("NETHERITE")) return 6.0;
		} else if (name.endsWith("_SHOVEL")) {
			if (name.startsWith("WOODEN") || name.startsWith("GOLDEN")) return 2.5;
			if (name.startsWith("STONE")) return 3.5;
			if (name.startsWith("IRON")) return 4.5;
			if (name.startsWith("DIAMOND")) return 5.5;
			if (name.startsWith("NETHERITE")) return 6.5;
		}
		return 1.0;
	}

	private void updateItemBuffs(@NotNull ItemStack item, int lvl) {
		if (lvl < 1 || lvl - 1 >= Constants.ENCHANT_MULTIPLIERS.size()) return;

		Material type = item.getType();
		String name = type.name();

		item.editMeta(meta -> {
			Double multiplier = Constants.ENCHANT_MULTIPLIERS.get(lvl - 1);
			if (multiplier == null) multiplier = 1.0;
			if (name.endsWith("_SWORD") || name.endsWith("_AXE")) {
				double baseDamage = getBaseDamage(type);
				if (baseDamage <= 0) return;

				meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);

				AttributeModifier modifier = new AttributeModifier(
						damageModifierKey,
						(baseDamage * multiplier) - 1,
						AttributeModifier.Operation.ADD_NUMBER,
						EquipmentSlotGroup.MAINHAND
				);
				meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, modifier);
			}
			else if (name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL")) {
				meta.removeAttributeModifier(Attribute.MINING_EFFICIENCY);
				double bonusMiningSpeed = (multiplier - 1.0) * 35.0;

				AttributeModifier modifier = new AttributeModifier(
						damageModifierKey,
						bonusMiningSpeed,
						AttributeModifier.Operation.ADD_NUMBER,
						EquipmentSlotGroup.MAINHAND
				);
				meta.addAttributeModifier(Attribute.MINING_EFFICIENCY, modifier);
			}
		});
	}

    private void applyLevelChange(@NotNull ItemStack item, int newLvl) {
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
		updateItemBuffs(item, newLvl);
    }

    public ResultType enchant(@NotNull Inventory inv) {
        return enchant(inv.getItem(13));
    }

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

    public void openInventory(Player p) {
        p.openInventory(new EnchantUserInterface(this).getInventory());
    }

	@Override
	public void init() {}

	public record EnchantChance(double success, double fail, double breakChance) {}

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