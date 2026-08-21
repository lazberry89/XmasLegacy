package org.lazberry.xmaslegacy.stock.commands.display;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.stock.Stock;
import org.lazberry.xmaslegacy.stock.StockManager;
import org.lazberry.xmaslegacy.stock.display.StockDisplayConfig;
import org.lazberry.xmaslegacy.stock.display.StockDisplayManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Commands(command = "display")
@Registry.Include(type = ServerType.MAIN)
public class DisplayCommand implements CommandExecutor, TabCompleter {
	private final Map<String, SubCommand> commands = new HashMap<>();
	private final StockManager sm;

	@Inject
	public DisplayCommand(StockDisplayManager sdm, StockManager sm, StockDisplayConfig sdc) {
		this.sm = sm;
		commands.put("spawn", new DisplayCommandSpawn(sm, sdm, sdc));
		commands.put("undo", new DisplayCommandUndo(sdm, sdc));
		commands.put("clear", new DisplayCommandClear(sdm, sdc));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (!p.isOp()) return true;

		if (args.length == 0) {
			InfoUtils.error(p, "사용법: /display <spawn|undo|clear> [인자...]");
			return true;
		}

		Optional.ofNullable(commands.get(args[0].toLowerCase()))
				.ifPresent(cmd -> cmd.execute(p, args));
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (args.length == 1) {
			return commands.keySet().stream()
					.filter(sub -> sub.startsWith(args[0].toLowerCase()))
					.toList();
		}

		if (args.length >= 2 && args[0].equalsIgnoreCase("spawn")) {
			return sm.getStocks().stream()
					.map(Stock::getName)
					.filter(name -> name.startsWith(args[args.length - 1]))
					.toList();
		}

		return List.of();
	}
}
