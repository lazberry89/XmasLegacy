package org.lazberry.xmaslegacy.collectors.drop;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Registry.Include(type = ServerType.MAIN)
public class DropsRepository implements Initiator {
	private final Map<CollectorLoot, ItemStack> lootItems = new HashMap<>();
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
		Arrays.stream(CollectorLoot.values())
				.forEach(loot -> lootItems.put(loot, createLootItem(loot)));
	}

	@Override
	public void close() {
		lootItems.clear();
	}

	private ItemStack createLootItem(CollectorLoot loot) {
		return ItemBuilder.of(plugin, loot.getMaterial())
				.setName(ColorUtils.chat(loot.getValue().getColorStr() + loot.getName()))
				.setTag(key, true)
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
		if (!isLootItem(item)) return null;

		return Arrays.stream(CollectorLoot.values())
				.filter(loot -> loot.getMaterial() == item.getType())
				.findFirst()
				.orElse(null);
	}

	public boolean isLootItem(ItemStack item) {
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
