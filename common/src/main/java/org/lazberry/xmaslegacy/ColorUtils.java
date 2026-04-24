package org.lazberry.xmaslegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtils {
	public static Component chat(String message) {
		if (message == null) return Component.empty();

		return LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("&#", "#"));
	}

	public static String toLegacy(Component component) {
		return LegacyComponentSerializer.legacySection().serialize(component);
	}
}

