package org.lazberry.xmaslegacy;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerManager;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;

@Registry.Exclude(type = ServerType.LOBBY)
public class RuleManager implements ServerManager {
	private final @NotNull List<String> badWords;

    public RuleManager() {
        this.badWords = new ArrayList<>(List.of("ㅅㅂ", "ㅄ", "시발", "장애", "지랄", "ㅈㄹ", "병신"));
	    if (this.badWords.isEmpty()) {
		    this.badWords.addAll(of("ㅅㅂ", "ㅄ", "시발", "장애", "지랄", "ㅈㄹ", "병신"));
	    } //TODO I/O Process is needed.
    }

	@Override
	public void init() {}

	public boolean checkBadWords(@NotNull String s) {
		return badWords.stream().anyMatch(s::contains);
	}

	public @NotNull String hideBadWords(@NotNull String message) {
		String processedMessage = message;
		for (String word : badWords)
			processedMessage = processedMessage.replace(word, "&k" + "#".repeat(word.length()) + "&r");
		return processedMessage;
	}

    public void addBadWordList(@NotNull String s) {
        this.badWords.add(s);
    }
    public void removeBadWordList(@NotNull String s) {
        this.badWords.remove(s);
    }
    public @NotNull List<String> getBadWordList() {
        return this.badWords;
    }
}
