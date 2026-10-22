package org.lazberry.xmaslegacy.framework;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ServerType {
	GLOBAL("global", true),
	LOBBY("lobby", true),
	MAIN("main", true),
	HUNTING("hunting", true),
	WILD("wild", true);

	private final @NotNull @Getter String name;
	private final @Getter boolean requiresGlobalInitializer;

	ServerType(@NotNull String name, boolean requiresGlobalInitializer) {
		this.name = name;
		this.requiresGlobalInitializer = requiresGlobalInitializer;
	}

	public static List<ServerType> getUserValidServers() {
		return List.of(MAIN, WILD);
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
			return ServerType.MAIN;
		}
	}
}
