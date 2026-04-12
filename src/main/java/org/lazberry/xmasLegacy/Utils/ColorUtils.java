package org.lazberry.xmasLegacy.Utils;

import org.bukkit.ChatColor;

public class ColorUtils {
	public static String chat(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}

