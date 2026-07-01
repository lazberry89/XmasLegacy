package org.lazberry.xmaslegacy.RoleSelection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.lazberry.xmaslegacy.Annotation.Commands;

@TestOnly
@Commands(command = "직업선택")
public class RoleSelectCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		if (!(commandSender instanceof Player p)) return true;
		RoleSelectInterface RSI = new RoleSelectInterface();
		p.openInventory(RSI.getInventory());
		return true;
	}
}
