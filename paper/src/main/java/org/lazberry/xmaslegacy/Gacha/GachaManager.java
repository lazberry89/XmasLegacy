package org.lazberry.xmaslegacy.Gacha;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public enum GachaManager {
	INSTANCE;

	private final @NotNull XmasLegacy plugin;
	private final @NotNull Map<String, Gacha> normalGachas = new HashMap<>();
	private final @NotNull Map<String, Gacha> highEndGachas = new HashMap<>();
	private final @NotNull Map<String, Gacha> chromaticBundle = new HashMap<>();
	private final @NotNull Map<String, Gacha> chromaticBox = new HashMap<>();

	private final @NotNull List<ItemStack> bundles = new ArrayList<>();

	GachaManager() {
		this.plugin = XmasLegacy.getInstance();
		appendBundles();
	}

	public void addGacha(String key, ItemStack item, GachaGrade grade, double chance, BundleType... types) {
		BundleType[] targetTypes = (types.length == 0) ? new BundleType[]{BundleType.NORMAL} : types;

		for (BundleType type : targetTypes) {
			Map<String, Gacha> targetMap = getGachaMaps(type);
			if (targetMap.containsKey(key)) {
				targetMap.get(key).setChance(chance);
			} else {
				targetMap.put(key, new Gacha(item, key, grade, chance));
			}
		}
	}

	public boolean removeGacha(String key, @NotNull BundleType type) {
		boolean value = getGachaMaps(type).containsKey(key);
		getGachaMaps(type).remove(key);
		return value;
	}

	public @Nullable Gacha getRandomItem(BundleType type) {
		Map<String, Gacha> targetMap = getGachaMaps(type);

		if (targetMap.isEmpty()) return null;

		double totalWeight = 0;
		for (Gacha gacha : targetMap.values()) {
			totalWeight += gacha.getChance();
		}

		double pivot = ThreadLocalRandom.current().nextDouble(0, totalWeight);

		double cumulativeWeight = 0;
		for (Gacha gacha : targetMap.values()) {
			cumulativeWeight += gacha.getChance();
			if (pivot <= cumulativeWeight) {
				return gacha;
			}
		}
		return null;
	}

	@Contract(value = "null, _ -> null", pure = true)
	public @Nullable Gacha getGacha(String key, BundleType type) {
		return getGachaMaps(type).get(key);
	}

	@Contract(pure = true)
	public @NotNull Map<String, Gacha> getGachaMaps(BundleType type) {
		switch (type) {
            case HIGH_END -> {return this.highEndGachas;}
			case CHROMATIC_BUNDLE -> {return this.chromaticBundle;}
			case CHROMATIC_BOX -> {return this.chromaticBox;}
			default -> {return this.normalGachas;}
		}
	}
	@Contract(pure = true)
	public @NotNull List<Gacha> getAll() {
		List<Gacha> gachas = new ArrayList<>();
        gachas.addAll(this.normalGachas.values());
		gachas.addAll(this.highEndGachas.values());
		gachas.addAll(this.chromaticBundle.values());
		gachas.addAll(this.chromaticBox.values());

		return gachas;
	}
	@Contract(pure = true)
	public @NotNull List<Gacha> getAllSortedByChance(BundleType type) {
		switch (type) {
			case HIGH_END -> {
				return highEndGachas.values().stream()
						.sorted((g1, g2) -> Double.compare(g2.getChance(), g1.getChance()))
						.toList();
			}
			case CHROMATIC_BUNDLE -> {
				return chromaticBundle.values().stream()
						.sorted((g1, g2) -> Double.compare(g2.getChance(), g1.getChance()))
						.toList();
			}
			case CHROMATIC_BOX -> {
				return chromaticBox.values().stream()
						.sorted((g1, g2) -> Double.compare(g2.getChance(), g1.getChance()))
						.toList();
			}
			default -> {
				return normalGachas.values().stream()
						.sorted((g1, g2) -> Double.compare(g2.getChance(), g1.getChance()))
						.toList();
			}
		}
	}

	public ItemStack Bundle() {
		return ItemBuilder.of(plugin, Material.BUNDLE)
				.setName(ColorUtils.chat("&c&l치장 번들"))
				.setLore(ColorUtils.chat("&7우클릭하여 번들을 열 수 있어요!"), ColorUtils.chat(String.format("&7(%s&7 이 아이템은 확률형 아이템을 포함합니다.)", Alert.YELLOW)))
				.hideAllFlags()
				.setTag("gacha", "NORMAL")
				.setMaxStackSize(16)
				.build().clone();
	}

	public ItemStack HighEndBundle() {
		return ItemBuilder.of(plugin, Material.ENDER_CHEST)
				.setName(ColorUtils.chat("&b&l고급 치장 번들"))
				.setLore(ColorUtils.chat("&7우클릭하여 번들을 열 수 있어요!"), ColorUtils.chat(String.format("&7(%s&7 이 아이템은 확률형 아이템을 포함합니다.)", Alert.YELLOW)))
				.hideAllFlags()
				.setGlint(true)
				.setTag("gacha", "HIGH_END")
				.setMaxStackSize(16)
				.build().clone();
	}

	public ItemStack ChromaticBundle() {
		return ItemBuilder.of(plugin, Material.PAPER)
				.setGlint(true)
				.setTag("gacha", "CHROMATIC_BUNDLE")
				.setName(ColorUtils.chat("&6&l크로마틱 번들"))
				.setLore(ColorUtils.chat("&7우클릭하여 번들을 열 수 있어요!"), ColorUtils.chat(String.format("&7(%s&7 이 아이템은 확률형 아이템을 포함합니다.)", Alert.YELLOW)), ColorUtils.chat(Alert.YELLOW + " 해당 번들은 추가 아이템을 포함합니다."))
				.setGlint(true)
				.setItemModel("")
				.setMaxStackSize(16)
				.hideAllFlags()
				.build().clone();
	}

	public ItemStack ChromaticBox() {
		return ItemBuilder.of(plugin, Material.CHEST)
				.setItemModel("")
				.setMaxStackSize(4)
				.setName(ColorUtils.chat("&6&l크로마틱 히든 꾸러미"))
				.setLore(ColorUtils.chat("&7우클릭하여 번들을 열 수 있어요!"), ColorUtils.chat(String.format("&7(%s&7 이 아이템은 확률형 아이템을 포함합니다.)", Alert.YELLOW)), ColorUtils.chat(Alert.XmasLegacy + "&7해당 상품은 특별 &6한정판 상품&r을 추가로 포함합니다."))
				.setGlint(true)
				.setTag("gacha", "CHROMATIC_BOX")
				.setItemModel("")
				.hideAllFlags()
				.build().clone();
	}
	 private void appendBundles() {
		this.bundles.add(Bundle());
		this.bundles.add(HighEndBundle());
		this.bundles.add(ChromaticBundle());
		this.bundles.add(ChromaticBox());
	 }

	 public @NotNull List<ItemStack> getBundles() {
		return List.copyOf(this.bundles);
	 }
}
