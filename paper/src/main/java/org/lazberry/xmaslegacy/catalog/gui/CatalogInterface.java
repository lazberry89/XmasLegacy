package org.lazberry.xmaslegacy.catalog.gui;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.catalog.CatalogManager;
import org.lazberry.xmaslegacy.catalog.CatalogProgress;
import org.lazberry.xmaslegacy.catalog.CatalogType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.FloodgateUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

/** One player's view of one catalog. Registered slots show the item, the rest stay locked. */
@Getter
public class CatalogInterface implements InventoryHolder {
	private final CatalogType type;
	private final Inventory inventory;
	/** The window art already draws a silhouette under every unregistered slot. */
	private final boolean drawn;

	public CatalogInterface(@NotNull Player player, @NotNull CatalogType type) {
		boolean mobile = FloodgateUtils.isFloodgate(player.getUniqueId());
		this.type = type;
		this.drawn = type.hasArt() && !mobile;
		this.inventory = Bukkit.createInventory(this, CatalogManager.SIZE, mobile ? Component.text(type.getDisplayName()) : type.title());
	}

	public void render(@NotNull ItemStack[] samples, @NotNull CatalogProgress progress) {
		for (int slot = 0; slot < CatalogManager.SIZE; slot++) {
			ItemStack sample = samples[slot];
			if (sample == null) inventory.setItem(slot, null);
			else if (progress.isRegistered(type, slot)) inventory.setItem(slot, sample.clone());
			else inventory.setItem(slot, drawn ? null : locked(sample));
		}
	}

	private static ItemStack locked(ItemStack sample) {
		return ItemBuilder.of(XmasLegacy.getInstance(), Material.GRAY_STAINED_GLASS_PANE)
				.name(sample.effectiveName())
				.lore(
						ColorUtils.chat("&8아직 등록하지 않았습니다."),
						ColorUtils.chat("&7아래에서 아이템을 클릭해 등록하세요."),
						ColorUtils.chat("&7Shift+클릭: 가진 아이템 모두 등록")
				)
				.build();
	}
}
