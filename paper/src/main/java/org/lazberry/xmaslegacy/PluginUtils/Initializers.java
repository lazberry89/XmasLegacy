package org.lazberry.xmaslegacy.PluginUtils;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.*;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerManager;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Registry
public enum Initializers implements ServerManager {
	INSTANCE;

	private final @NotNull Map<ServerType, ServerInitializer> initializers = new EnumMap<>(ServerType.class){{
		put(ServerType.GLOBAL, new GlobalInitializer());
		put(ServerType.MAIN, new MainInitializer());
		put(ServerType.HUNTING, new HuntingInitializer());
		put(ServerType.LOBBY, new LobbyInitializer());
	}};

    Initializers() {}

	@Override
	public void init() {

	}

	/**
	 * Method casts instance to target ServerInitializer
	 * @return Generic cast Initializer returned. But not recommended in cast instance.
	 * @param <I> ServerInitializer instance.
	 * @param type what kind of server to get Initializer
	 * @param flag Only for overloading method.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated(forRemoval = true, since = "1.21.11")
	public <I extends ServerInitializer> @NotNull I getInitializer(@NotNull ServerType type, boolean flag) {
		return Objects.requireNonNull((I) this.initializers.get(type));
	}

	public @NotNull ServerInitializer getInitializer(@NotNull ServerType type) {
		return this.initializers.get(type);
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
	public <I extends ServerInitializer> @NotNull I getInitializer(ServerType type, Class<I> clazz) throws ClassCastException {
		return clazz.cast(this.initializers.get(type));
	}


}
