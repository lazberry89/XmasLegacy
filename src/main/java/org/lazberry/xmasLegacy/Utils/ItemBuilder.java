package org.lazberry.xmasLegacy.Utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
	private final ItemStack item;
	private final ItemMeta meta;

	// 1. 생성자: 재료(Material)만 먼저 받습니다.
	public ItemBuilder(Material material) {
		this.item = new ItemStack(material);
		this.meta = item.getItemMeta();
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
	public ItemBuilder hideFlags() {
		if (meta != null) {
			// 아이템의 모든 부가 정보(공격력, 인챈트 정보, 내구도 등)를 숨깁니다.
			meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
			meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DESTROYS);
			meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON);
		}
		return this;
	}

	public ItemStack build() {
		if (meta != null) {
			item.setItemMeta(meta);
		}
		return item;
	}

	public static ItemBuilder of(Material material) {
		return new ItemBuilder(material);
	}
}
