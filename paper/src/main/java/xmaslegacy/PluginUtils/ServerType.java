package xmaslegacy.PluginUtils;

import org.jetbrains.annotations.NotNull;

public enum ServerType {
    LOBBY("lobby", new LobbyInitializer()),
    MAIN("main", new MainInitializer()),;

    private final @NotNull String name;
	private final @NotNull ServerInitializer initializer;

    ServerType(@NotNull String name, @NotNull ServerInitializer initializer) {
        this.name = name;
		this.initializer = initializer;
    }

    public @NotNull String str() {
        return this.name;
    }
	public static @NotNull ServerType getServerType(@NotNull String name) {
		try {return ServerType.valueOf(name.toUpperCase());}
		catch (IllegalArgumentException e) {return ServerType.LOBBY;}
	}

	@SuppressWarnings("unchecked")
	public <I extends ServerInitializer> @NotNull I getInitializer() {
		return (I) this.initializer;
	}
}
