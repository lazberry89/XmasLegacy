package org.lazberry.xmaslegacy.Cosmetics;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Registry
public enum CosmeticManager implements ServerManager {
	INSTANCE;

	private final @NotNull Map<String, Cosmetics> equippedCosmetics = new HashMap<>();

	CosmeticManager() {}

	public void addCosmetics(@NotNull ItemStack model, @NotNull String name) {
		CosmeticType type = name.contains("head") ? CosmeticType.HEAD : CosmeticType.BODY;
		Cosmetics cosmetic = new Cosmetics(model, name, type);
		equippedCosmetics.put(name, cosmetic);
	}

	public void deleteCosmetics(@NotNull Cosmetics cosmetic) {
		equippedCosmetics.remove(cosmetic.getName());
	}

	public @Nullable Cosmetics getEquippedCosmetics(@NotNull String name) {
		return equippedCosmetics.get(name);
	}

	public @NotNull List<String> getCosmeticsName() {
		return equippedCosmetics.keySet().stream().toList();
	}

	@Override
	public void init() {}
}
