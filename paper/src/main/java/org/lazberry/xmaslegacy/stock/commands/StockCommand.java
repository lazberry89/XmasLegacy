package org.lazberry.xmaslegacy.stock.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.stock.StockManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Registry.Include(type = ServerType.MAIN)
@Commands(command = "stock", aliases = "주식")
public class StockCommand implements CommandExecutor, TabCompleter {
	private final Map<String, SubCommand> commands = new HashMap<>();
	private final StockManager sm;

	@Inject
	public StockCommand(StockManager sm) {
		this.sm = sm;
		commands.put("sell", new StockCommandSell(sm));
		commands.put("판매", new StockCommandSell(sm));
		commands.put("list", new StockCommandList(sm));
		commands.put("목록", new StockCommandList(sm));
		commands.put("buy", new StockCommandBuy(sm));
		commands.put("구매", new StockCommandBuy(sm));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		if (!(commandSender instanceof Player p)) return true;
		if (strings.length == 0) {
			InfoUtils.error(p, "올바른 명령어 사용법이 아닙니다.");
			return true;
		}
		var subCommand = commands.get(strings[0]);
		if (subCommand == null) {
			InfoUtils.error(p, "올바른 명령어 사용법이 아닙니다.");
			return true;
		}
		subCommand.execute(p, strings);
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		return List.of();
	}
}
