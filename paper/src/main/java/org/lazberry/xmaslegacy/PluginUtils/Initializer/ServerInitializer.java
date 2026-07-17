package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Reflection;
import org.lazberry.xmaslegacy.PluginUtils.Initializers;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.ServerType;

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

	/**
	 * use to check what kind of server that where this plugin is loaded.
	 * @param plugin Plugin instance
	 * @return Returns current server's Server type.
	 */
	static @NotNull ServerType getServerType(@NotNull XmasLegacy plugin) {
		plugin.saveDefaultConfig();
		return ServerType.getServerType(plugin.getConfig().getString("server-type", "main"));
	}
}
