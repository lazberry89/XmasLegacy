package org.lazberry.xmaslegacy.PluginUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.*;

@Slf4j
public enum ServerType {
	GLOBAL("global", new GlobalInitializer(), true),
    LOBBY("lobby", new LobbyInitializer(), false),
    MAIN("main", new MainInitializer(), true),
	HUNTING("hunting", new HuntingInitializer(), true);

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
	@Contract("_ -> !null")
	public static @NotNull ServerType getServerType(@NotNull String name) {
		try {return ServerType.valueOf(name.toUpperCase());}
		catch (IllegalArgumentException e) {
			log.error("Not valid server type in Config: {}, Starting as MAIN", name);
			return ServerType.MAIN;
		}
	}

	/**
	 * Method casts instance to target ServerInitializer
	 * @return Generic cast Initializer returned. But not recommended in cast instance.
	 * @param <I> ServerInitializer instance.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated(forRemoval = true, since = "1.21.11")
	public <I extends ServerInitializer> @NotNull I getInitializer(boolean flag) {
		return (I) this.initializer;
	}

	public @NotNull ServerInitializer getInitializer() {
		return this.initializer;
	}

	/**
	 * Method casts instance to target ServerInitializer class of param
	 * <pre>{@code
	 * MainInitializer main = ServerType.MAIN.getInitializer(MainInitializer.class);
	 * }</pre>
	 * @param clazz target initializer's class
	 * @return cast instance of ServerInitializer class
	 * @param <I> generic instance
	 * @throws ClassCastException throws ClassCastException
	 */
	public <I extends ServerInitializer> @NotNull I getInitializer(Class<I> clazz) throws ClassCastException {
		return clazz.cast(this.initializer);
	}
}
