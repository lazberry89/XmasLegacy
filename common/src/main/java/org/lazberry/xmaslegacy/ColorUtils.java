package org.lazberry.xmaslegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtils {

	private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
			.character('&')
			.hexColors()
			.build();

	public static Component chat(String message) {
		if (message == null) return Component.empty();

		return SERIALIZER.deserialize(message);
	}

	public static String toLegacy(Component component) {
		return SERIALIZER.serialize(component);
	}
}

