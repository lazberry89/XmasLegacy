package org.lazberry.xmaslegacy.PlayerUtils;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.Env.Cookie;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TempBag implements InventoryHolder {
	private final @NotNull Inventory inv;
	private final @NotNull @Getter UUID owner;

	public TempBag(@NotNull UUID uuid) {
		this.owner = uuid;
		this.inv = Bukkit.createInventory(this, Constants.BAG_SIZE,ColorUtils.chat("&c&l[임시 보관함]"));
		this.inv.setItem(0, basicFood());
	}
	public @NotNull ItemStack basicFood() {
		return Cookie.cookie(Constants.FREE_COOKIE_COUNT);
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
