package xmaslegacy.Ranks.RankingCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.RankType;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.Ranks.RankingSystem;
import xmaslegacy.Utils.InfoUtils;

import java.util.Arrays;
import java.util.List;

@Commands(command = "rank")
public class RankingCommand implements CommandExecutor, TabCompleter {

	public RankingCommand() {}

	///rank self/<Player> <type>
	///rank task <Type> <Boolean>

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length < 2) {
			InfoUtils.error(p, "유효한 사용법이 아닙니다!");
			return true;
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("self")) new RankingCommandSelf().execute(p, args);
			else new RankingCommandPlayer().execute(p, args);
		}
		else if (args.length == 3) {
			if (!p.isOp()) {
				InfoUtils.error(p, "관리자용 명령어에요!");
				return true;
			}
			if (args[0].equalsIgnoreCase("task")) new RankingCommandTask().execute(p, args);
			else InfoUtils.error(p, "유효하지 않은 명령어입니다!");
		}
		else InfoUtils.error(p, "유효하지 않은 명령어입니다!");
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (args.length == 1) return List.of("self", "task", "<name>");
		if (args.length == 2) return Arrays.stream(RankType.values())
				.map(Enum::name)
				.map(String::toUpperCase)
				.toList();
		if (args.length == 3 && args[1].equalsIgnoreCase("task")) return List.of("on", "off");
		return null;
	}
}
