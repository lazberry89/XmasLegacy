package org.lazberry.xmaslegacy.PlayerUtils;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.food.AgeableCrops;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ConsumableClass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TempBag implements InventoryHolder {
	private final @NotNull Inventory inv;
	@EqualsAndHashCode.Include
	private final @Getter UUID owner;

	public TempBag(UUID uuid) {
		this.owner = uuid;
		this.inv = Bukkit.createInventory(this, Constants.BAG_SIZE,ColorUtils.chat("&c&l[임시 보관함]"));
		this.inv.setItem(0, basicFood());
	}

	public @NotNull ItemStack basicFood() {
		return AgeableCrops.SunFlowerBread();
	}

	@Override
	public @NotNull Inventory getInventory() {
		return this.inv;
	}

    @CanIgnoreReturnValue
	public @NotNull List<ItemStack> addItem(@Nullable ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return new ArrayList<>();

		ItemStack toAdd = item.clone();

		Map<Integer, ItemStack> left = this.inv.addItem(toAdd);
		if (left.isEmpty()) return new ArrayList<>();
        return Lists.newArrayList(left.values());
	}
}
