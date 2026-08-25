package org.lazberry.xmaslegacy.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ColorUtils {

	private static final @NotNull LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.builder()
			.character('&')
			.hexColors()
			.build();

	private static final @NotNull LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.builder()
			.character(LegacyComponentSerializer.SECTION_CHAR)
			.hexColors()
			.build();

	/**
	 * Utility method that changes string value to Colored Component.
	 * @param message string value to change
	 * @return Component result
	 */
	public static @NotNull Component chat(@Nullable String message) {
		if (message == null) return Component.empty();

		return AMPERSAND_SERIALIZER.deserialize(message);
	}

	/**
	 * Changes string input to colored String message. But not recommended.
	 * @param message string to change
	 * @return String result
	 */
	@Deprecated(since = "26.1")
	public static @NotNull String chatStr(@Nullable String message) {
		if (message == null) return "";

		return SECTION_SERIALIZER.serialize(AMPERSAND_SERIALIZER.deserialize(message));
	}

	/**
	 * This method changes componented value to pure String value.
	 * @param component Component value to change
	 * @return String result
	 */
	public static @NotNull String toLegacy(@NotNull Component component) {
		return AMPERSAND_SERIALIZER.serialize(component);
	}
}

