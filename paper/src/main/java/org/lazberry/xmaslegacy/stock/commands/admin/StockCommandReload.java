package org.lazberry.xmaslegacy.stock.commands.admin;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.stock.StockConfig;

public record StockCommandReload(StockConfig sc) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 2) {
			InfoUtils.error(player, "Usage: /stock admin reload");
			return;
		}
		sc.reloadConfig().whenComplete((v, e) -> {
			if (e == null) InfoUtils.info(player, "Config reloaded!");
			else InfoUtils.error(player, "Failed to reload config!");
		});
	}
}
