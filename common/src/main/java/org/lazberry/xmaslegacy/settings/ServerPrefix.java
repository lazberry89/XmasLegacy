package org.lazberry.xmaslegacy.settings;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface ServerPrefix {
	@NotNull Component prefix();
	int ordinal();
	@NotNull String name();

	static @NotNull ServerPrefix parse(String name, ServerPrefix def) {
		try {return Tier.valueOf(name);} catch (IllegalArgumentException ignored) {}
		try {return RoleMastery.valueOf(name);} catch (IllegalArgumentException ignored) {}
		try {return MissionPrefix.valueOf(name);} catch (IllegalArgumentException ignored) {}
		return def;
	}
}
