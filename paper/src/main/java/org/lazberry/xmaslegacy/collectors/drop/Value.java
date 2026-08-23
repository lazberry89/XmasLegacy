package org.lazberry.xmaslegacy.collectors.drop;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Value {
	TRASH("&8&l"),
	RARE("&a&l"),
	PRECIOUS("&b&l"),
	NOBLE("&6&l"),
	SPECIAL("&e&l");

	private final String colorStr;
}
