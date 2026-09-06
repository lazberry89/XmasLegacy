package org.lazberry.xmaslegacy.teleporter.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.teleporter.logic.TeleporterManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record TeleporterCommandRemove(TeleporterManager tm) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length >= 2) {
			String id = args[1];
			if (tm.remove(id)) {
				InfoUtils.info(player, "Removed teleporter command with id '{}'", id);
			} else {
				InfoUtils.info(player, "No such teleporter with id '{}'.", id);
			}
		}
	}
}
