package org.lazberry.xmasLegacy;

import org.lazberry.xmasLegacy.Utils.ColorUtils;

import java.util.List;

public class RuleManager {
	private final List<String> bad = List.of("ㅅㅂ", "ㅆㅂ", "시발", "ㅄ", "ㅂㅅ", "병신", "련아", "련이", "장애", "새끼", "고아");

	public boolean checkBadWords(String s) {
		return bad.stream().anyMatch(s::contains);
	}

	public String hideBadWords(String message) {
		String processedMessage = message;

		for (String word : bad) {
			if (processedMessage.contains(word)) {
				processedMessage = processedMessage.replace(word, ColorUtils.chat("&k" + "#".repeat(word.length()) + "&r"));
			}
		}

		return processedMessage;
	}
}
