package org.lazberry.xmaslegacy.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Commands;
import org.lazberry.xmaslegacy.user.UserSaveManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.FloodgateUtils;

@Commands(command = "0947345")
@Components.Exclude(type = ServerType.LOBBY)
public class UserLoadCommand implements CommandExecutor {
	private final UserSaveManager us;

	@Inject
	public UserLoadCommand(@NotNull UserSaveManager us) {
		this.us = us;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;

		var uuid = p.getUniqueId();
		us.load(uuid, p.getName(), FloodgateUtils.isFloodgate(uuid));
		return true;
	}
}
