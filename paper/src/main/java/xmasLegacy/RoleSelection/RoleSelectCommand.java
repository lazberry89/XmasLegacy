package xmasLegacy.RoleSelection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import xmasLegacy.XmasLegacy;

@SuppressWarnings("ClassCanBeRecord")
@TestOnly
public class RoleSelectCommand implements CommandExecutor {
	private final XmasLegacy plugin;

	public RoleSelectCommand(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		if (!(commandSender instanceof Player p)) return true;
		RoleSelectInterface RSI = new RoleSelectInterface(plugin);
		p.openInventory(RSI.getInventory());
		return true;
	}
}
