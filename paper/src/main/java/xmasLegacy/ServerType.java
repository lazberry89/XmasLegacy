package xmasLegacy;

import org.jetbrains.annotations.NotNull;

public enum ServerType {
    LOBBY("lobby"),
    MAIN("main"),
    HUNTING("hunting"),
    BOSS("boss");

    private final String name;

    ServerType(String name) {
        this.name = name;
    }

    public @NotNull String str() {
        return this.name;
    }
}
