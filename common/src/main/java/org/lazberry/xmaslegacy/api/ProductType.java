package org.lazberry.xmaslegacy.api;

import lombok.Getter;

@Getter
public enum ProductType {
	SEASON_PASS("season_pass", false),
	STARTER_PACK("starter_pack", false),
	SUPPORTER("supporter", false),;

	private final String key;
	private final boolean canDuplicated;

	ProductType(String key, boolean canDuplicated) {
		this.key = key;
		this.canDuplicated = canDuplicated;
	}
}
