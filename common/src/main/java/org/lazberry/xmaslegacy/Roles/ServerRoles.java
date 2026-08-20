package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.NotNull;

public enum ServerRoles implements Role {
    USER("유저"),
    FARMER("농부"),
    MINER("광부"),
    FISHERMAN("어부"),
    KNIGHT("기사"),
    BUILDER("건축가"),
    FIGHTER("싸움꾼");

    ServerRoles(String name) {
        this.name = name;
    }

    private final String name;

    @Override
    public @NotNull String getKor() {
        return name;
    }
}
