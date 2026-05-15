package org.lazberry.xmaslegacy.settings;

import net.kyori.adventure.text.Component;
import org.lazberry.xmaslegacy.ColorUtils;

public enum MissionPrefix implements ServerPrefix {
	FIRST_JOINER(ColorUtils.chat("FIRST JOINER")),
	VIP(ColorUtils.chat("VIP")),
	VVIP(ColorUtils.chat("VVIP")),
	BOSS_HUNTER(ColorUtils.chat("BOSS HUNTER"));

	private final Component prefix;

	MissionPrefix(Component prefix) {
		this.prefix = prefix;
	}

	@Override
	public Component prefix() {
		return this.prefix;
	}
}
