package org.lazberry.xmaslegacy.PluginUtils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.GlobalInitializer;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LobbyInitializer;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.MainInitializer;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.ServerInitializer;

public enum ServerType {
	GLOBAL("global", new GlobalInitializer(), true),
    LOBBY("lobby", new LobbyInitializer(), false),
    MAIN("main", new MainInitializer(), true);

    private final @NotNull String name;
	private final @NotNull ServerInitializer initializer;
	private final @Getter boolean requiresGlobalInitializer;

    ServerType(@NotNull String name, @NotNull ServerInitializer initializer, boolean global) {
        this.name = name;
		this.initializer = initializer;
		this.requiresGlobalInitializer = global;
    }

	/**
	 * This value is same as Saved Config value.
	 * @return String of server name.
	 */
    public @NotNull String configValue() {
        return this.name;
    }

	/**
	 * Method to change String to ServerType.
	 * @param name get String from config, and this method changes String to ServerType.
	 * @return same value of ServerType is returned.
	 */
	public static @NotNull ServerType getServerType(@NotNull String name) {
		try {return ServerType.valueOf(name.toUpperCase());}
		catch (IllegalArgumentException e) {return ServerType.MAIN;}
	}

	/**
	 *
	 * @return Generic cast Initializer returned. But not recommended in cast instance.
	 * @param <I> ServerInitializer instance.
	 */
	@SuppressWarnings("unchecked")
	public <I extends ServerInitializer> @NotNull I getInitializer() {
		return (I) this.initializer;
	}
}
