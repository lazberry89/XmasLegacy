package org.lazberry.xmasLegacy.Utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemBuilder {
	private final ItemStack item;
	private final ItemMeta meta;
    private XmasLegacy plugin;

	// 1. 생성자: 재료(Material)만 먼저 받습니다.
	public ItemBuilder(XmasLegacy plugin, Material material) {
		this.item = new ItemStack(material);
		this.meta = item.getItemMeta();
        this.plugin = plugin;
	}

	// 2. 이름 설정
	public ItemBuilder setName(String name) {
		if (meta != null) {
			meta.displayName(ComponentChanger.comp(name));
		}
		return this;
	}

	public ItemBuilder setLore(String... lore) {
		if (meta != null) {
			List<Component> loreList = new ArrayList<>();
			for (String line : lore) {
				loreList.add(ComponentChanger.comp(line));
			}
			meta.lore(loreList);
		}
		return this;
	}

	public ItemBuilder setGlint(boolean glint) {
		if (meta != null) {
			meta.setEnchantmentGlintOverride(glint);
		}
		return this;
	}
	public ItemBuilder setHeadOwner(OfflinePlayer p) {
		if (item.getType() == Material.PLAYER_HEAD && meta != null) {
			SkullMeta sm = (SkullMeta) meta;
			sm.setOwningPlayer(p);
			item.setItemMeta(sm);
			return this;
		} else {
			return null;
		}
	}

    public ItemBuilder hideAllFlags() {
        if (meta != null) {
            for (ItemFlag flag : ItemFlag.values()) {
                meta.addItemFlags(flag);
            }
        }
        return this;
    }
    public ItemBuilder addAttribute(Attribute attribute, double amount, AttributeModifier.Operation operation) {
        if (meta != null) {
            // 1.21+ 에서는 유니크한 Key가 필요합니다.
            NamespacedKey key = new NamespacedKey(this.plugin, attribute.getKey().getKey());
            AttributeModifier modifier = new AttributeModifier(key, amount, operation, EquipmentSlotGroup.MAINHAND);

            meta.addAttributeModifier(attribute, modifier);
        }
        return this;
    }
    public ItemBuilder addAttribute(Attribute attribute, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        if (meta != null) {
            // 1.21+ 에서는 유니크한 Key가 필요합니다.
            NamespacedKey key = new NamespacedKey(this.plugin, attribute.getKey().getKey());
            AttributeModifier modifier = new AttributeModifier(key, amount, operation, slot);

            meta.addAttributeModifier(attribute, modifier);
        }
        return this;
    }

    /**
     * 공격력을 설정하는 전용 편의 메서드 (가장 많이 씀)
     */
    public ItemBuilder setAttackDamage(double damage) {
        return addAttribute(Attribute.ATTACK_DAMAGE, damage, AttributeModifier.Operation.ADD_NUMBER);
    }

	public ItemBuilder setArmorState(double state) {
		return addAttribute(Attribute.ARMOR, state, AttributeModifier.Operation.ADD_NUMBER);
	}

    public ItemBuilder setUnbreakable() {
        if (meta != null) {
            meta.setUnbreakable(true);
        }
        return this;
    }
	/**
	 * 특정 마법을 부여합니다.
	 * @param enchantment 마법 종류
	 * @param level 레벨
	 * @return ItemBuilder
	 */
	public ItemBuilder addEnchant(Enchantment enchantment, int level) {
		if (meta != null) {
			meta.addEnchant(enchantment, level, true);
		}
		return this;
	}

	/**
	 * 여러 마법을 한꺼번에 부여합니다.
	 * @param enchantments 마법 Map
	 * @return ItemBuilder
	 */
	public ItemBuilder addEnchants(Map<Enchantment, Integer> enchantments) {
		if (meta != null) {
			enchantments.forEach((enchant, level) -> meta.addEnchant(enchant, level, true));
		}
		return this;
	}
    public ItemStack setAmount(int i) {
        item.setAmount(i);
        return item;
    }
    /**
     * 1.21.4+ 버전의 새로운 Item Model 기능을 설정합니다.
     * 정수형 ID 대신 NamespacedKey(String)를 사용하여 모델을 지정할 수 있습니다.
     * @param modelKey 모델의 키 (예: "my_plugin:knight_sword")
     */
    public ItemBuilder setItemModel(String modelKey) {
        if (meta != null) {
            // NamespacedKey를 생성하여 아이템 모델을 직접 지정합니다.
            NamespacedKey key = NamespacedKey.fromString(modelKey);
            if (key != null) {
                meta.setItemModel(key);
            }
        }
        return this;
    }

    public ItemBuilder setCustomModelData(Integer data) {
        if (meta != null) {
            meta.setCustomModelData(data);
        }
        return this;
    }

    /**
     * NamespacedKey를 이용해 아이템에 숨겨진 데이터(태그)를 심습니다.
     * @param key 저장할 키 이름
     * @param value 저장할 값
     */
    public ItemBuilder setTag(String key, String value) {
        if (meta != null) {
            NamespacedKey nsk = new NamespacedKey(this.plugin, key);
            meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, value);
        }
        return this;
    }

	public ItemStack build() {
		if (meta != null) {
			item.setItemMeta(meta);
		}
		return item;
	}

	public static ItemBuilder of(XmasLegacy plugin, Material material) {
		return new ItemBuilder(plugin, material);
	}
}
