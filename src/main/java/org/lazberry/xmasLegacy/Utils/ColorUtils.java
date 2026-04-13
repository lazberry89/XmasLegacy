package org.lazberry.xmasLegacy.Utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
	private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

	public static String chat(String message) {
		if (message == null) return null;

		// 1. HEX 코드 변환 (&#54daf4 -> 특수 색상 객체)
		Matcher matcher = HEX_PATTERN.matcher(message);
		StringBuilder buffer = new StringBuilder();

		while (matcher.find()) {
			matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
		}
		message = matcher.appendTail(buffer).toString();

		// 2. 기존의 & 코드 변환
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}

