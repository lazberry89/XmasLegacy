package xmasLegacy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.UserManager;

@Commands(command = "0947345")
public class UserLoadCommand implements CommandExecutor {
	private final UserManager um;

	public UserLoadCommand() {
		this.um = UserManager.getInstance();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		um.load(p.getUniqueId(), p.getName());
		return true;
	}
}
