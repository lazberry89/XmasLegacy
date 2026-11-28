package org.lazberry.xmaslegacy.catalog.gui;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.catalog.CatalogManager;
import org.lazberry.xmaslegacy.catalog.CatalogType;

import java.util.Arrays;

/** Admin window: the items put here are the catalog's slots. */
@Getter
public class CatalogSampleEditor implements InventoryHolder {
	private final CatalogType type;
	private final Inventory inventory;
	private final ItemStack[] original;

	public CatalogSampleEditor(@NotNull CatalogType type, @NotNull ItemStack[] samples) {
		this.type = type;
		this.inventory = Bukkit.createInventory(this, CatalogManager.SIZE, Component.text("[샘플] " + type.getDisplayName()));
		this.inventory.setContents(samples);
		this.original = contents();
	}

	public ItemStack[] contents() {
		return CatalogManager.cloneContents(inventory.getContents());
	}

	public boolean isChanged() {
		return !Arrays.equals(original, contents());
	}
}
