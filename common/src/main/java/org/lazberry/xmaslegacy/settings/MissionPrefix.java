package org.lazberry.xmaslegacy.settings;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;

public enum MissionPrefix implements ServerPrefix {
	FIRST_JOINER("FIRST JOINER"),
	VIP("VIP"),
	VVIP("VVIP"),
	BOSS_HUNTER("BOSS HUNTER"),
	MERRY_CHRISTMAS("Christmas!");

	private final @NotNull String prefix;

	MissionPrefix(@NotNull String prefix) {
		this.prefix = prefix;
	}

	@Override
	public @NotNull Component prefix() {
		return ColorUtils.chat(this.prefix);
	}
}
