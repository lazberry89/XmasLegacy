package org.lazberry.xmaslegacy.Utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface SubCommand {
    void execute(@NotNull Player player, @NotNull String @NotNull...args);
}
