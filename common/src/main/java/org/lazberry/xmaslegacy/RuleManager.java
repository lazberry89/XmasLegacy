package org.lazberry.xmaslegacy;

import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;

public enum RuleManager {
	INSTANCE(List.of("ㅅㅂ", "ㅄ", "시발", "장애", "지랄", "ㅈㄹ", "병신"));

	private final List<String> badWords;

    RuleManager(List<String> initialWords) {
        this.badWords = new ArrayList<>(initialWords);
	    if (this.badWords.isEmpty()) {
		    this.badWords.addAll(of("ㅅㅂ", "ㅄ", "시발", "장애", "지랄", "ㅈㄹ", "병신"));
	    }
    }

	public boolean checkBadWords(String s) {
		return badWords.stream().anyMatch(s::contains);
	}

	public String hideBadWords(String message) {
		String processedMessage = message;

		for (String word : badWords) {
			if (processedMessage.contains(word)) {
				processedMessage = processedMessage.replace(word, "&k" + "#".repeat(word.length()) + "&r");
			}
		}

		return processedMessage;
	}

    public void addBadWordList(String s) {
        this.badWords.add(s);
    }
    public void removeBadWordList(String s) {
        this.badWords.remove(s);
    }
    public List<String> getBadWordList() {
        return this.badWords;
    }
}
