package org.lazberry.xmaslegacy.utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface SubCommand {

    /**
     * Defines what action that current subCommand does.
     * @param player who used command.
     * @param args args of typed command.
     */
    void execute(@NotNull Player player, @NotNull String @NotNull...args);
}
