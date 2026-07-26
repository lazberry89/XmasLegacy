package org.lazberry.xmaslegacy.HuntingZone.ZoneCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.HuntingZone.HuntingZoneManager;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.HuntingZone.ZoneType;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.*;

@Commands(command = "zone")
@Registry.Include(type = ServerType.HUNTING)
public class ZoneCommand implements CommandExecutor, TabCompleter {
	private final @NotNull Map<String, SubCommand> subCommand = new HashMap<>();

	@Inject
	public ZoneCommand(@NotNull HuntingZoneManager hzm) {
		this.subCommand.put("alive", new ZoneCommandAlive(hzm));
		this.subCommand.put("delete", new ZoneCommandDelete(hzm));
		this.subCommand.put("despawn", new ZoneCommandDespawn(hzm));
		this.subCommand.put("expand", new ZoneCommandExpand(hzm));
		this.subCommand.put("get", new ZoneCommandGet(hzm));
		this.subCommand.put("spawn", new ZoneCommandSpawn(hzm));
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
