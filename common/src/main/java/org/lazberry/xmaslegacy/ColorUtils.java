package org.lazberry.xmaslegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ColorUtils {

	private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.builder()
			.character('&')
			.hexColors()
			.build();

	private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.builder()
			.character(LegacyComponentSerializer.SECTION_CHAR)
			.hexColors()
			.build();

	public static @NotNull Component chat(String message) {
		if (message == null) return Component.empty();

		return AMPERSAND_SERIALIZER.deserialize(message);
	}

	public static @NotNull String chatStr(@Nullable String message) {
		if (message == null) return "";

		return SECTION_SERIALIZER.serialize(AMPERSAND_SERIALIZER.deserialize(message));
	}

	public static @NotNull String toLegacy(@NotNull Component component) {
		return AMPERSAND_SERIALIZER.serialize(component);
	}
}

