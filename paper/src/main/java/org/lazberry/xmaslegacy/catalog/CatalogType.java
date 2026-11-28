package org.lazberry.xmaslegacy.catalog;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.utils.ColorUtils;

public enum CatalogType {
	XMAS("크리스마스 도감", null, Material.SNOWBALL),
	FARM("농사 도감", "catalog_farm", Material.WHEAT),
	FISH("낚시 도감", null, Material.TROPICAL_FISH),
	MINE("광물 도감", null, Material.DIAMOND),
	HUNT("사냥 도감", null, Material.BONE);

	private final String displayName;
	/** Oraxen glyph drawn over the window (XmasResourcePack/oraxen/glyphs/xmaslegacy_gui.yml), null until it has art. */
	private final @Nullable String glyph;
	private final Material icon;

	CatalogType(String displayName, @Nullable String glyph, Material icon) {
		this.displayName = displayName;
		this.glyph = glyph;
		this.icon = icon;
	}

	public @NotNull String getDisplayName() {
		return displayName;
	}

	public @NotNull Material getIcon() {
		return icon;
	}

	/** True when {@link #title()} draws window art that already shows every unregistered slot. */
	public boolean hasArt() {
		return glyph != null && Bukkit.getPluginManager().isPluginEnabled("Oraxen");
	}

	/**
	 * Window title: -8 onto the window edge, the 176px-wide art (advance 176, its last column is empty),
	 * -168 back to the normal title spot (x=8), then the name in the vanilla title color.
	 * Bedrock (Floodgate) viewers have no resource pack: callers should use {@link #getDisplayName()} for them.
	 */
	public @NotNull Component title() {
		if (!hasArt())
			return Component.text(displayName);
		return ColorUtils.minimessage("<white><shift:-8><glyph:" + glyph + "><shift:-168><color:#404040>" + displayName);
	}
}
