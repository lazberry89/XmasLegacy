package xmasLegacy.Cosmetics;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosmeticManager {
	private final Map<String, Cosmetics> equippedCosmetics = new HashMap<>();

	public void addCosmetics(ItemStack model, String name) {
		Cosmetics cosmetic = new Cosmetics(model, name);
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
