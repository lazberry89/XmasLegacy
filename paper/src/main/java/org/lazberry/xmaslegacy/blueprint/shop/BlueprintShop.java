package org.lazberry.xmaslegacy.blueprint.shop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.blueprint.BlueprintGrade;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlueprintShop implements InventoryHolder {
	private final Set<ItemStack> lowItems = ConcurrentHashMap.newKeySet();
	private final Set<ItemStack> mediumItems = ConcurrentHashMap.newKeySet();
	private final Set<ItemStack> highItems = ConcurrentHashMap.newKeySet();
	private final XmasLegacy plugin;
	private final Inventory inv;
	private BlueprintGrade currentPage = BlueprintGrade.LOW;

	public BlueprintShop(XmasLegacy plugin, Collection<BluePrint> blueprints) {
		this.plugin = plugin;
		this.inv = Bukkit.createInventory(this, 54, ColorUtils.chat("&9&l도면 상점"));

		setContents(blueprints);
		refreshInventory();
	}

	private void refreshInventory() {
		clearPartly();

		var items = getItemsByGrade(currentPage).toArray(ItemStack[]::new);
		for (int i = 0; i < Math.min(items.length, 45); i++) {
			inv.setItem(i, items[i]);
		}

		var bg = InventoryHelper.background();
		for (int i = 45; i < 54; i++) {
			inv.setItem(i, bg);
		}
		inv.setItem(47, iconBuilder(BlueprintGrade.LOW));
		inv.setItem(49, iconBuilder(BlueprintGrade.MEDIUM));
		inv.setItem(51, iconBuilder(BlueprintGrade.HIGH));
	}

	private Set<ItemStack> getItemsByGrade(BlueprintGrade grade) {
		return switch (grade) {
			case LOW -> lowItems;
			case MEDIUM -> mediumItems;
			case HIGH -> highItems;
		};
	}

	private void clearPartly() {
		for (int i = 0; i < 45; i++) {
			inv.clear(i);
		}
	}

	public void setContents(Collection<BluePrint> blueprints) {
		if (blueprints.isEmpty()) return;
		blueprints.forEach(b -> {
			var grade = b.getGrade();
			switch (grade) {
				case LOW -> lowItems.add(b.getItem());
				case MEDIUM -> mediumItems.add(b.getItem());
				case HIGH -> highItems.add(b.getItem());
			}
		});
	}

	public void changePage(BlueprintGrade newPage) {
		currentPage = newPage;
		refreshInventory();
	}

	private ItemStack iconBuilder(BlueprintGrade grade) {
		boolean isCurrent = (this.currentPage == grade);
		return ItemBuilder.of(plugin, grade.getIcon())
				.setName(grade.getDisplayName())
				.setGlint(isCurrent)
				.build();
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inv;
	}
}
