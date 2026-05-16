package org.lazberry.xmaslegacy.settings;

import net.kyori.adventure.text.Component;
import org.lazberry.xmaslegacy.ColorUtils;

public enum MissionPrefix implements ServerPrefix {
	FIRST_JOINER("FIRST JOINER"),
	VIP("VIP"),
	VVIP("VVIP"),
	BOSS_HUNTER("BOSS HUNTER"),
	MERRY_CHRISTMAS("Christmas!");

	private final String prefix;

	MissionPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Component prefix() {
		return ColorUtils.chat(this.prefix);
	}
}
