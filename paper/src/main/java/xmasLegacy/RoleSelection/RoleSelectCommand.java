package xmasLegacy.RoleSelection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import xmasLegacy.XmasLegacy;

@TestOnly
public class RoleSelectCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		if (!(commandSender instanceof Player p)) return true;
		RoleSelectInterface RSI = new RoleSelectInterface();
		p.openInventory(RSI.getInventory());
		return true;
	}
}
