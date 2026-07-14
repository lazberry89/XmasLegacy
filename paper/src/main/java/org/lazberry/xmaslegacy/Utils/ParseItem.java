package org.lazberry.xmaslegacy.Utils;

import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParseItem {

	@ApiStatus.Internal
	private ParseItem() {
		throw new UnsupportedOperationException("Utility Class");
	}

	/**
	 * This method parses String value to target Material instance.
	 * @param value Target material's display name
	 * <pre>{@code
	 * ParseItem.parse("glass");
	 * }</pre>
	 * @return if not exists, returns null else return Target material value.
	 */
	@Contract("null -> null")
	public static @Nullable Material parse(@Nullable String value) {
		if (value == null) return null;
		try {
			return Material.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * get String from param and parse into material, and if fail returns default item.
	 * @param value Target material's display name
	 * @param defaultItem default item to return when fail to parse.
	 * <pre>{@code
	 * ParseItem.parse("glass", Material.GLASS);
	 * }</pre>
	 * @return returns target value if valid else returns default item.
	 */
	@Contract("null, _ -> param2")
	public static @NotNull Material parse(@Nullable String value, @NotNull Material defaultItem) {
		if (value == null) return defaultItem;
		Material result;
		try {
			result = Material.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			result = defaultItem;
		}
		return result;
	}
}
