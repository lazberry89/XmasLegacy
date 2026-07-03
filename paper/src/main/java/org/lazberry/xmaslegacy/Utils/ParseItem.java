package org.lazberry.xmaslegacy.Utils;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParseItem {

	public static @Nullable Material parse(@Nullable String value) {
		if (value == null) return null;
		try {
			return Material.valueOf(value.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}

	public static @NotNull Material parse(@Nullable String value, @NotNull Material defaultItem) {
		if (value == null) return defaultItem;
		Material result;
		try {
			result = Material.valueOf(value.toUpperCase());
		} catch (Exception e) {
			result = defaultItem;
		}
		return result;
	}
}
