package org.lazberry.xmaslegacy;

public enum Prefix {
	XmasLegacy("&c&l[&a&lX&c&lm&a&la&c&ls&a&lL&c&le&a&lg&c&la&a&lc&c&ly&a&l]&f"),
	RED("&c&l[!]&f"),
	YELLOW("&e&l[!]&f"),
    GREEN("&a&l[!]&f"),;

	Prefix(String prefix) {
		this.prefix = prefix;
	}

	private final String prefix;

	@Override
	public String toString() {
		return this.prefix;
	}
}
