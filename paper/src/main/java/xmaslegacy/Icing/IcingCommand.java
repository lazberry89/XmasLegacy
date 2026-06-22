package xmaslegacy.Icing;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.XmasLegacy;

import java.util.List;

public record IcingCommand(@NotNull IcingSystem system) implements CommandExecutor, TabCompleter {

	public IcingCommand(IcingSystem system) {
		this.system = system;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull ...args) {
		var plugin = XmasLegacy.getInstance();
		var um = UserManager.INSTANCE;
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) {
			plugin.infoMsg(InfoLevel.ERROR, p, "관리자용 명령어에요!");
			return true;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("task")) {
				if (system.isTaskRunning()) {
					system.stopTask();
					plugin.infoMsg(InfoLevel.INFO, p, "빙결 시스템을 정지하였습니다.");
				} else {
					system.startTask(plugin);
					plugin.infoMsg(InfoLevel.INFO, p, "빙결 시스템을 시작하였습니다.");
				}
			}
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		return List.of();
	}
}
