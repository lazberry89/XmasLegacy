package xmaslegacy.HuntingZone.ZoneCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.HuntingZone.ZoneType;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

import java.util.*;

@Commands(command = "zone")
public class ZoneCommand implements CommandExecutor, TabCompleter {
	private final @NotNull Map<String, SubCommand> subCommand = new HashMap<>();

	public ZoneCommand() {
		this.subCommand.put("alive", new ZoneCommandAlive());
		this.subCommand.put("delete", new ZoneCommandDelete());
		this.subCommand.put("despawn", new ZoneCommandDespawn());
		this.subCommand.put("expand", new ZoneCommandExpand());
		this.subCommand.put("get", new ZoneCommandGet());
		this.subCommand.put("spawn", new ZoneCommandSpawn());
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) {
			InfoUtils.warn(p, "관리자용 명령어에요!");
			return true;
		}
		if (args.length == 0) return false;
		SubCommand sub = this.subCommand.get(args[0].toLowerCase());
		if (sub == null) {
			InfoUtils.error(p, "잘못된 명령어입니다.");
			return true;
		}
		sub.execute(p, args);
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		List<String> result = new ArrayList<>();

		if (args.length == 1) {
			this.subCommand.keySet()
					.stream().filter(sub -> sub.startsWith(args[0].toLowerCase())).forEach(result::add);
		}

		if (args.length == 2) Arrays.stream(ZoneType.values())
					.map(Enum::name)
					.filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
					.forEach(result::add);
		return result;
	}
}
