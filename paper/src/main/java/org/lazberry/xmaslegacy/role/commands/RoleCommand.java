package org.lazberry.xmaslegacy.role.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.roles.HiddenRoles;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.*;

@Commands(command = "role")
@Registry.Exclude(type = ServerType.LOBBY)
public class RoleCommand implements CommandExecutor, TabCompleter {
	private final Map<String, SubCommand> commands = new HashMap<>();

	@Inject
	public RoleCommand(UserManager userManager) {
		commands.put("set", new RoleCommandSet(userManager));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (!p.isOp()) {
			InfoUtils.error(p, "관리자 전용 명령어에요!");
			return true;
		}
		var subCommand = commands.get(args[0].toLowerCase());
		if (subCommand == null) {
			InfoUtils.error(p, "올바르지 않은 명령어에요!");
			return true;
		}
		subCommand.execute(p, args);
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (args.length == 1) return List.of("set");
		else if (args.length == 2) return null;
		else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
			List<String> list = new ArrayList<>();
			Arrays.stream(ServerRoles.values()).map(ServerRoles::name).forEach(list::add);
			Arrays.stream(HiddenRoles.values()).map(HiddenRoles::name).forEach(list::add);
			return list;
		}
		return List.of();
	}
}
