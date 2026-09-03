package org.lazberry.xmaslegacy.mining.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.mining.MineConfig;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record MineCommandReload(MineConfig config) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 1) {
            InfoUtils.warn(player, "Reloading config..");
            config.loadAll().whenComplete((v, e) -> {
               if (e == null) InfoUtils.info(player, "Done!");
               else InfoUtils.error(player, ColorUtils.chat("Failed to reload config file. Check the log."), e);
            });
        }
    }
}
