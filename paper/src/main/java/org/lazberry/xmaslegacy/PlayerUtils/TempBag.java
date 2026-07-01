package org.lazberry.xmaslegacy.PlayerUtils;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TempBag implements InventoryHolder {
	private final XmasLegacy plugin;
	private Inventory inv;
	private final UUID uuid;

	public TempBag(XmasLegacy plugin, UUID uuid) {
		this.plugin = plugin;
		this.uuid = uuid;
		this.inv = Bukkit.createInventory(this, Constants.BAG_SIZE,ColorUtils.chat("&c&l[임시 보관함]"));
		this.inv.setItem(0, basicFood());
	}
	public ItemStack basicFood() {
		ItemStack a =  ItemBuilder.of(plugin, Material.POTATO)
				.setName(ColorUtils.chat("&c&l라즈베리 쿠키"))
				.setLore(ColorUtils.chat("&8이런식으로 표시됩니다!"))
				.setGlint(true)
				.hideAllFlags()
				.addAttribute(Attribute.LUCK, 0.1, AttributeModifier.Operation.ADD_NUMBER)
				.build();
		a.setAmount(Constants.FREE_COOKIE_COUNT);
		return a.clone();
	}

	@Override
	public @NotNull Inventory getInventory() {
		return this.inv;
	}

    @CanIgnoreReturnValue
	public @NotNull List<ItemStack> addItem(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return new ArrayList<>();

		ItemStack toAdd = item.clone();

		Map<Integer, ItemStack> left = this.inv.addItem(toAdd);
		if (left.isEmpty()) return new ArrayList<>();
        return Lists.newArrayList(left.values());
	}
}
