package org.lazberry.xmaslegacy.mining.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.mining.MineConfig;
import org.lazberry.xmaslegacy.mining.logics.MineCreateManager;
import org.lazberry.xmaslegacy.mining.logics.MineManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Commands(command = "mine")
@Registry.Include(type = ServerType.MAIN)
public class MineCommands implements CommandExecutor, TabCompleter {
	private final Map<String, SubCommand> commands;
	private final MineManager mm;

	@Inject
	public MineCommands(MineCreateManager mcm, MineManager mm, MineConfig config) {
		this.mm = mm;
		this.commands = Map.of(
				"create", new MineCommandCreate(mcm, mm),
				"remove", new MineCommandRemove(mm),
				"reload", new MineCommandReload(config)
		);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (!p.isOp()) {
			InfoUtils.error(p, "You don't have permission to use this command!");
			return true;
		}
		if (args.length < 1) {
			p.getInventory().addItem(mm.tool());
			return true;
		}
		Optional.ofNullable(commands.get(args[0].toLowerCase())).ifPresentOrElse(
				c -> c.execute(p, args), () ->
			InfoUtils.error(p, "Wrong usage of command")
		);
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!sender.isOp()) return Collections.emptyList();
		if (args.length == 1) return List.of("create", "remove");
		if (args.length == 2) return List.of("internal", "external");
		return Collections.emptyList();
	}
}
