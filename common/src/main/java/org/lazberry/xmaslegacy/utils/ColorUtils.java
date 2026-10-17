package org.lazberry.xmaslegacy.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ColorUtils {

	public static final String BLACK = "&0";
	public static final String DARK_BLUE = "&1";
	public static final String DARK_GREEN = "&2";
	public static final String DARK_AQUA = "&3";
	public static final String DARK_RED = "&4";
	public static final String DARK_PURPLE = "&5";
	public static final String GOLD = "&6";
	public static final String GRAY = "&7";
	public static final String DARK_GRAY = "&8";
	public static final String BLUE = "&9";
	public static final String GREEN = "&a";
	public static final String AQUA = "&b";
	public static final String RED = "&c";
	public static final String LIGHT_PURPLE = "&d";
	public static final String YELLOW = "&e";
	public static final String WHITE = "&f";

	public static final String OBFUSCATED = "&k";
	public static final String BOLD = "&l";
	public static final String STRIKETHROUGH = "&m";
	public static final String UNDERLINE = "&n";
	public static final String ITALIC = "&o";
	public static final String RESET = "&r";

	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();


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
	 * Parse minimessage formatted String value to color applied Component message.
	 * @param message string value to apply.
	 * @return color applied Component message
	 */
	public static Component minimessage(String message) {
		return MINI_MESSAGE.deserialize(message);
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

