package xmasLegacy.Gacha;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused, FieldCanBeLocal")
public class GachaManager {
	private final XmasLegacy plugin;
	private final Map<String, Gacha> gachas = new HashMap<>();

	public GachaManager(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	public void addGacha(String key, ItemStack item, GachaGrade grade, double chance) {
		if (gachas.containsKey(key)) {
			gachas.get(key).setChance(chance);
		} else {
			gachas.put(key, new Gacha(item, key, grade, chance));
		}
	}

	public void removeGacha(String key) {
		gachas.remove(key);
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
	@Contract(value = "null -> null", pure = true)
	public @Nullable Gacha getGacha(String key) {
		return this.gachas.get(key);
	}
	@Contract(pure = true)
	public @NotNull Map<String, Gacha> getGachaMap() {
		return this.gachas;
	}
	@Contract(pure = true)
	public @NotNull List<Gacha> getAll() {
		return this.gachas.values().stream().toList();
	}
	@Contract(pure = true)
	public @NotNull List<Gacha> getAllSortedByChance() {
		return gachas.values().stream()
				.sorted((g1, g2) -> Double.compare(g2.getChance(), g1.getChance()))
				.toList();
	}

	public ItemStack Bundle() {
		return ItemBuilder.of(plugin, Material.BUNDLE)
				.setName(ColorUtils.chat("&c&l치장 번들"))
				.setLore(ColorUtils.chat("&7우클릭하여 번들을 열 수 있어요!"), ColorUtils.chat(String.format("&7(%s&7 이 아이템은 확률형 아이템을 포함합니다.)", Prefix.YELLOW)))
				.hideAllFlags()
				.setGlint(true)
				.setMaxStackSize(16)
				.build().clone();
	}

	public ItemStack HighEndBundle() {
		return ItemBuilder.of(plugin, Material.ENDER_CHEST)
				.setName(ColorUtils.chat("&b&l고급 치장 번들"))
				.setLore(ColorUtils.chat("&7우클릭하여 번들을 열 수 있어요!"), ColorUtils.chat(String.format("&7(%s&7 이 아이템은 확률형 아이템을 포함합니다.)", Prefix.YELLOW)))
				.hideAllFlags()
				.setGlint(true)
				.setMaxStackSize(16)
				.build().clone();
	}
}
