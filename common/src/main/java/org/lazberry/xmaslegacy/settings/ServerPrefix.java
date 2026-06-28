package org.lazberry.xmaslegacy.settings;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface ServerPrefix {
	@NotNull Component prefix();
	int ordinal();
	@NotNull String name();
}
