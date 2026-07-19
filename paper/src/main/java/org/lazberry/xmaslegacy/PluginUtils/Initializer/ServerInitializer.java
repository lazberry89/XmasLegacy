package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;

public interface ServerInitializer {
	/*
	  Make enable/disable logic to get plugin instance via Param to minimalize plugin field.
	 */

	/**
	 * Enable logic of Each Initializer.
	 * @param plugin XmasLegacy instance
	 */
	void initiate(@NotNull XmasLegacy plugin);

	/**
	 * Disabling logic of Each Initializer.
	 * @param plugin XmasLegacy instance
	 */
	default void shutdown(@NotNull XmasLegacy plugin) {}
}
