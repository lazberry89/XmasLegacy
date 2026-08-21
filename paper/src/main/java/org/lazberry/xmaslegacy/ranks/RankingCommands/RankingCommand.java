package org.lazberry.xmaslegacy.ranks.RankingCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ranks.RankManager;
import org.lazberry.xmaslegacy.ranks.RankingTask;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Arrays;
import java.util.List;

@Commands(command = "rank")
@Registry.Exclude(type = ServerType.LOBBY)
public class RankingCommand implements CommandExecutor, TabCompleter {
	private final @NotNull XmasLegacy plugin;
	private final @NotNull UserManager um;
	private final @NotNull RankManager rm;
	private final @NotNull RankingTask task;

	@Inject
	public RankingCommand(@NotNull XmasLegacy plugin, @NotNull UserManager um, @NotNull RankManager rm, @NotNull RankingTask task) {
		this.plugin = plugin;
		this.um = um;
		this.rm = rm;
		this.task = task;
	}

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
			if (args[0].equalsIgnoreCase("self")) new RankingCommandSelf(um, rm).execute(p, args);
			else new RankingCommandPlayer(um, rm).execute(p, args);
		}
		else if (args.length == 3) {
			if (!p.isOp()) {
				InfoUtils.error(p, "관리자용 명령어에요!");
				return true;
			}
			if (args[0].equalsIgnoreCase("task")) new RankingCommandTask(plugin, task).execute(p, args);
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
