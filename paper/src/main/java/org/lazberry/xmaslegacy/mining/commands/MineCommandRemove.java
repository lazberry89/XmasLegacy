package org.lazberry.xmaslegacy.mining.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.mining.logics.MineManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record MineCommandRemove(MineManager mm) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 2) {
            String div = args[1];
            switch (div.toLowerCase()) {
                case "internal" -> {
                    mm.resetInternal();
                    InfoUtils.info(player, "Internal field reset.");
                }
                case "external" -> {
                    mm.resetExternal();
                    InfoUtils.info(player, "External field reset.");
                }
                default -> InfoUtils.error(player, "Select between 'internal' or 'external'");
            }
        } else InfoUtils.error(player, "Wrong usage of command");
    }
}
