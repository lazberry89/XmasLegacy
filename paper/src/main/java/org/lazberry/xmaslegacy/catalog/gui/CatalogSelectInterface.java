package org.lazberry.xmaslegacy.catalog.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.catalog.CatalogManager;
import org.lazberry.xmaslegacy.catalog.CatalogProgress;
import org.lazberry.xmaslegacy.catalog.CatalogType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

@Getter
public class CatalogSelectInterface implements InventoryHolder {
	private final Inventory inventory;

	public CatalogSelectInterface(@NotNull CatalogManager manager, @NotNull CatalogProgress progress) {
		this.inventory = Bukkit.createInventory(this, 9, ColorUtils.chat("&0도감"));
		for (int i = 0; i < 9; i++) inventory.setItem(i, InventoryHelper.background());

		for (CatalogType type : CatalogType.values()) {
			int total = manager.total(type);
			boolean complete = manager.isComplete(type, progress);
			inventory.setItem(type.ordinal() * 2, ItemBuilder.of(XmasLegacy.getInstance(), type.getIcon())
					.name(ColorUtils.chat("&6&l" + type.getDisplayName()))
					.lore(
							ColorUtils.chat(total == 0 ? "&8준비 중입니다."
									: (complete ? "&a완성! " : "&7등록 ") + "&f" + manager.registered(type, progress) + " &7/ " + total),
							ColorUtils.chat("&7도감에서 아이템을 클릭해 등록하고,"),
							ColorUtils.chat("&7Shift+클릭으로 한번에 등록하세요.")
					)
					.hideAllFlags()
					.setGlint(complete)
					.build());
		}
	}

	public static @Nullable CatalogType typeAt(int slot) {
		CatalogType[] types = CatalogType.values();
		return slot >= 0 && slot % 2 == 0 && slot / 2 < types.length ? types[slot / 2] : null;
	}
}
