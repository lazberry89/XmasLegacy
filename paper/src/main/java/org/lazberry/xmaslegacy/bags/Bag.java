package org.lazberry.xmaslegacy.bags;

import com.google.common.collect.Lists;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.food.AgeableCrops;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventorySerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@ConsumableClass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Bag implements InventoryHolder {
	@EqualsAndHashCode.Include
	private final UUID owner;
	private final Inventory inv;

	public Bag(UUID uuid) {
		this(uuid, new ItemStack[]{AgeableCrops.SunFlowerBread()});
	}

	public Bag(UUID uuid, ItemStack[] contents) {
		this.inv = Bukkit.createInventory(this, Constants.BAG_SIZE, ColorUtils.chat("&c&l[임시 보관함]"));
		this.owner = uuid;
		this.inv.setContents(contents);
	}

	public String serializeContents() {
		return InventorySerializer.serializeContents(getContents());
	}

	public ItemStack[] getContents() {
		return inv.getContents();
	}

	@Override
	public @NotNull Inventory getInventory() {
		return this.inv;
	}

	public List<ItemStack> addItem(@Nullable ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return new ArrayList<>();

		ItemStack toAdd = item.clone();

		Map<Integer, ItemStack> left = this.inv.addItem(toAdd);
		if (left.isEmpty()) return new ArrayList<>();
        return Lists.newArrayList(left.values());
	}
}
