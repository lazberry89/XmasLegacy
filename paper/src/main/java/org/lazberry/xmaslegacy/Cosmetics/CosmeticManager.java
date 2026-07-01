package org.lazberry.xmaslegacy.Cosmetics;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum CosmeticManager {
	INSTANCE;

	private final Map<String, Cosmetics> equippedCosmetics = new HashMap<>();

	CosmeticManager() {}

	public void addCosmetics(ItemStack model, String name) {
		CosmeticType type = name.contains("head") ? CosmeticType.HEAD : CosmeticType.BODY;
		Cosmetics cosmetic = new Cosmetics(model, name, type);
		equippedCosmetics.put(name, cosmetic);
	}

	public void deleteCosmetics(Cosmetics cosmetic) {
		equippedCosmetics.remove(cosmetic.getName());
	}

	public @Nullable Cosmetics getEquippedCosmetics(String name) {
		return equippedCosmetics.get(name);
	}

	public List<String> getCosmeticsName() {
		return equippedCosmetics.keySet().stream().toList();
	}
}
