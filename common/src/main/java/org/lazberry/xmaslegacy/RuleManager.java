package org.lazberry.xmaslegacy;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Registry.Include(type = ServerType.GLOBAL)
public class RuleManager {
	private volatile Pattern cachedPattern;
	private final Set<String> badWords = new CopyOnWriteArraySet<>();

	public RuleManager() {
		this(List.of("ㅅㅂ", "ㅄ", "시발", "장애", "지랄", "ㅈㄹ", "병신"));
	}

	protected RuleManager(@NotNull Collection<? extends String> words) {
		this.badWords.addAll(words);
		recompilePattern();
	}

	private void recompilePattern() {
		if (badWords.isEmpty()) {
			this.cachedPattern = null;
			return;
		}
		String regex = badWords.stream()
				.map(Pattern::quote)
				.collect(Collectors.joining("|"));

		this.cachedPattern = Pattern.compile(regex);
	}

	public boolean checkBadWords(@NotNull String s) {
		Pattern pattern = this.cachedPattern;
		if (pattern == null || s.isEmpty()) return false;

		return pattern.matcher(s).find();
	}

	public @NotNull String hideBadWords(@NotNull String message) {
		Pattern pattern = this.cachedPattern;
		if (pattern == null || message.isEmpty()) return message;

		Matcher matcher = pattern.matcher(message);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String word = matcher.group();
			String replacement = "&k" + "#".repeat(word.length()) + "&r";
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public boolean add(String s) {
		if (s == null || s.isBlank()) return false;
		boolean added = badWords.add(s);
		if (added) recompilePattern();
		return added;
	}

	public  boolean addAll(Collection<? extends String> values) {
		return badWords.addAll(values);
	}

	public boolean remove(String s) {
		if (s == null) return false;
		boolean removed = this.badWords.remove(s);
		if (removed) recompilePattern();
		return removed;
	}

	public @NotNull Collection<? extends String> getBadWordList() {
		return Collections.unmodifiableCollection(badWords);
	}
}