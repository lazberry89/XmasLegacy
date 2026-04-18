package org.lazberry.xmasLegacy.PlayerUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;
import org.lazberry.xmasLegacy.Utils.ItemBuilder;
import org.lazberry.xmasLegacy.XmasLegacy;

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
		this.inv = Bukkit.createInventory(this, 27, ComponentChanger.comp("&c&l[임시 보관함]"));
		this.inv.setItem(0, basicFood());
	}
	public ItemStack basicFood() {
		ItemStack a =  ItemBuilder.of(plugin, Material.POTATO)
				.setName(ColorUtils.chat("&4&l라즈베리 쿠키"))
				.setLore(ColorUtils.chat("&8이런식으로 표시됩니다!"))
				.setGlint(true)
				.hideAllFlags()
				.addAttribute(Attribute.LUCK, 0.1, AttributeModifier.Operation.ADD_NUMBER)
				.build();
		a.setAmount(10);
		return a.clone();
	}

	@Override
	public @NotNull Inventory getInventory() {
		return this.inv;
	}

	public boolean addItem(ItemStack item, int amount) {
		if (item == null || item.getType() == Material.AIR) return true;

		ItemStack toAdd = item.clone();
		toAdd.setAmount(amount);

		Map<Integer, ItemStack> left = this.inv.addItem(toAdd);
		return left.isEmpty();
	}
}
