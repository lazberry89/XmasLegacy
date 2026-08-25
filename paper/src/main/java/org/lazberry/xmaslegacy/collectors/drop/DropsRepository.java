package org.lazberry.xmaslegacy.collectors.drop;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Registry.Include(type = ServerType.MAIN)
public class DropsRepository implements Initiator {
	private final Map<CollectorLoot, ItemStack> lootItems = new HashMap<>();
	private final Map<Value, List<CollectorLoot>> lootsByValue = new EnumMap<>(Value.class);

	private final XmasLegacy plugin;
	private final NamespacedKey key;
	private final NamespacedKey price;
	private final NamespacedKey weight;

	@Inject
	public DropsRepository(XmasLegacy plugin) {
		this.plugin = plugin;
		this.key = KeyUtils.get("loot");
		this.price = KeyUtils.get("price");
		this.weight = KeyUtils.get("weight");
	}

	@Override
	public void init() {
		Arrays.stream(Value.values()).forEach(v -> lootsByValue.put(v, new ArrayList<>()));
		Arrays.stream(CollectorLoot.values()).forEach(loot -> {
			lootItems.put(loot, createLootItem(loot));
			lootsByValue.get(loot.getValue()).add(loot);
		});
	}

	@Override
	public void close() {
		lootItems.clear();
		lootsByValue.clear();
	}

	public List<ItemStack> getRandomDrops(int count, Difficulty difficulty) {
		List<ItemStack> result = new ArrayList<>(count);
		if (count == 0) return result;

		for (int i = 0; i < count; i++) {
			Value rolledValue = rollValueByDifficulty(difficulty);
			List<CollectorLoot> candidates = lootsByValue.get(rolledValue);

			if (candidates == null || candidates.isEmpty()) {
				CollectorLoot[] allLoots = CollectorLoot.values();
				CollectorLoot fallback = allLoots[ThreadLocalRandom.current().nextInt(allLoots.length)];
				result.add(getLootItem(fallback));
				continue;
			}

			CollectorLoot selected = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
			result.add(getLootItem(selected));
		}

		return result;
	}

	public Map<Value, Integer> getValueWeights(Difficulty difficulty) {
		Map<Value, Integer> weights = new EnumMap<>(Value.class);

		switch (difficulty) {
			case PEACEFUL -> setWeights(weights, 65, 25, 10, 0, 0);
			case HORROR -> setWeights(weights, 40, 28, 18, 10, 4);
			default -> setWeights(weights, 50, 30, 15, 5, 0);
		}

		return weights;
	}

	private void setWeights(Map<Value, Integer> map, int... values) {
		Value[] tiers = Value.values();
		for (int i = 0; i < tiers.length && i < values.length; i++) {
			map.put(tiers[i], values[i]);
		}
	}

	private Value rollValueByDifficulty(Difficulty difficulty) {
		Map<Value, Integer> weights = getValueWeights(difficulty);
		int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
		if (totalWeight <= 0) return Value.values()[0];

		int random = ThreadLocalRandom.current().nextInt(totalWeight);
		int currentSum = 0;

		for (Map.Entry<Value, Integer> entry : weights.entrySet()) {
			currentSum += entry.getValue();
			if (random < currentSum) {
				return entry.getKey();
			}
		}

		return Value.values()[0];
	}

	private ItemStack createLootItem(CollectorLoot loot) {
		return ItemBuilder.of(plugin, loot.getMaterial())
				.setName(ColorUtils.chat(loot.getValue().getColorStr() + " " + loot.getName()))
				.setTag(key, PersistentDataType.STRING, loot.getName())
				.setTag(price, PersistentDataType.INTEGER, loot.getPrice())
				.setTag(weight, PersistentDataType.INTEGER, loot.getWeight())
				.hideAllFlags()
				.setGlint(loot.getValue().equals(Value.NOBLE) || loot.getValue().equals(Value.SPECIAL))
				.build();
	}

	public ItemStack getLootItem(CollectorLoot loot) {
		return lootItems.get(loot).clone();
	}

	public @Nullable CollectorLoot getCollectorLoot(ItemStack item) {
		if (item == null || item.getType().isAir()) return null;
		if (!isLootItem(item)) return null;

		return Arrays.stream(CollectorLoot.values())
				.filter(loot ->
						loot.getName().equals(KeyUtils.get(item, key, PersistentDataType.STRING)))
				.findFirst()
				.orElse(null);
	}

	public boolean isLootItem(ItemStack item) {
		if (item == null || item.getType().isAir()) return false;
		return KeyUtils.hasKey(item, key);
	}

	public int getPrice(@Nullable ItemStack item) {
		if (!isLootItem(item)) return 0;

		Integer singlePrice = KeyUtils.get(item, price, PersistentDataType.INTEGER);
		return singlePrice != null ? singlePrice * item.getAmount() : 0;
	}

	public int getWeight(@Nullable ItemStack item) {
		if (!isLootItem(item)) return 0;

		Integer singleWeight = KeyUtils.get(item, weight, PersistentDataType.INTEGER);
		return singleWeight != null ? singleWeight * item.getAmount() : 0;
	}
}