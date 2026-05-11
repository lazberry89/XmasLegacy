package xmasLegacy.Gacha;

import org.bukkit.inventory.ItemStack;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GachaManager {
	private final XmasLegacy plugin;
	private final Map<ItemStack, Gacha> gachas = new HashMap<>();

	public GachaManager(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	public void addGacha(ItemStack item, double chance) {
		if (gachas.containsKey(item)) {
			gachas.get(item).setChance(chance);
		} else {
			gachas.put(item, new Gacha(item, chance));
		}
	}

	public void removeGacha(ItemStack item) {
		gachas.remove(item);
	}

	public ItemStack getRandomItem() {
		if (gachas.isEmpty()) return null;

		double totalWeight = 0;
		for (Gacha gacha : gachas.values()) {
			totalWeight += gacha.getChance();
		}

		double pivot = ThreadLocalRandom.current().nextDouble(0, totalWeight);

		double cumulativeWeight = 0;
		for (Gacha gacha : gachas.values()) {
			cumulativeWeight += gacha.getChance();
			if (pivot <= cumulativeWeight) {
				return gacha.getItem().clone();
			}
		}

		return null;
	}
}
