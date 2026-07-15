package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import org.jetbrains.annotations.NotNull;
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
	void enable(@NotNull XmasLegacy plugin);

	/**
	 * Disabling logic of Each Initializer.
	 * @param plugin XmasLegacy instance
	 */
	default void disable(@NotNull XmasLegacy plugin) {}

	/**
	 * use to check what kind of server that where this plugin is loaded.
	 * @param plugin Plugin instance
	 * @return Returns current server's Server type.
	 */
	static @NotNull ServerType getServerType(@NotNull XmasLegacy plugin) {
		plugin.saveDefaultConfig();
		return ServerType.getServerType(plugin.getConfig().getString("server-type", "main"));
	}

	/**
	 * Only called by onEnable in XmasLegacy.class.
	 * @param plugin Plugin instance(XmasLegacy.class)
	 */
	static void initiate(@NotNull XmasLegacy plugin) {
		ServerType serverType = getServerType(plugin);

		plugin.getSLF4JLogger().info("Initializing {}", serverType.name());
		Initializers.INSTANCE.getInitializer(serverType).enable(plugin);
		if (serverType.isRequiresGlobalInitializer()) Initializers.INSTANCE.getInitializer(ServerType.GLOBAL, GlobalInitializer.class).enable(plugin);
	}

	/**
	 * Only called by onDisable in XmasLegacy.class.
	 * @param plugin Plugin instance(XmasLegacy.class)
	 */
	static void shutdown(@NotNull XmasLegacy plugin) {
		ServerType serverType = getServerType(plugin);

		plugin.getSLF4JLogger().info("Shutting down {}", serverType.name());
		Initializers.INSTANCE.getInitializer(serverType).disable(plugin);
		if (serverType.isRequiresGlobalInitializer()) Initializers.INSTANCE.getInitializer(ServerType.GLOBAL, GlobalInitializer.class).disable(plugin);
	}
}
