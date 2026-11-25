package org.lazberry.xmaslegacy.catalog;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.utils.ColorUtils;

public enum CatalogType {
	XMAS("크리스마스 도감", null),
	FARM("농사 도감", "catalog_farm"),
	FISH("낚시 도감", null),
	MINE("광물 도감", null),
	HUNT("사냥 도감", null);

	private final String displayName;
	/** Oraxen glyph drawn over the window (XmasResourcePack/oraxen/glyphs/xmaslegacy_gui.yml), null until it has art. */
	private final @Nullable String glyph;

	CatalogType(String displayName, @Nullable String glyph) {
		this.displayName = displayName;
		this.glyph = glyph;
	}

	public @NotNull String getDisplayName() {
		return displayName;
	}

	/**
	 * Window title: -8 onto the window edge, the 176px-wide art (advance 176, its last column is empty),
	 * -168 back to the normal title spot (x=8), then the name in the vanilla title color.
	 * Bedrock (Floodgate) viewers have no resource pack: callers should use {@link #getDisplayName()} for them.
	 */
	public @NotNull Component title() {
		if (glyph == null || !Bukkit.getPluginManager().isPluginEnabled("Oraxen"))
			return Component.text(displayName);
		return ColorUtils.minimessage("<white><shift:-8><glyph:" + glyph + "><shift:-168><color:#404040>" + displayName);
	}
}
