package org.lazberry.xmaslegacy.PluginUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Commands;
import org.lazberry.xmaslegacy.User.UserSaveManager;

@Commands(command = "0947345")
public class UserLoadCommand implements CommandExecutor {
	private final UserSaveManager us;

	public UserLoadCommand() {
		this.us = UserSaveManager.INSTANCE;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		us.load(p.getUniqueId(), p.getName());
		return true;
	}
}
