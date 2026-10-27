package org.lazberry.xmaslegacy.location;

import org.jetbrains.annotations.NotNull;

public enum DestinationType {
    MAIN("main"),
    LOBBY("lobby"),
    PORT("port");

    private final @NotNull String name;

    DestinationType(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String toString() {
        return this.name;
    }
}
