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
import org.lazberry.xmaslegacy.stock.Stock;
import org.lazberry.xmaslegacy.stock.StockManager;

import java.util.*;

@Registry.Include(type = ServerType.MAIN)
@Commands(command = "stock", aliases = "주식")
public class StockCommand implements CommandExecutor, TabCompleter {
	private final Map<String, SubCommand> commands = new HashMap<>();
	private final StockManager sm;

	@Inject
	public StockCommand(StockManager sm) {
		this.sm = sm;
		var sell = new StockCommandSell(sm);
		var list = new StockCommandList(sm);
		var buy = new StockCommandBuy(sm);
		var info = new StockCommandInfo(sm);
		commands.put("sell", sell);
		commands.put("판매", sell);
		commands.put("list", list);
		commands.put("목록", list);
		commands.put("buy", buy);
		commands.put("구매", buy);
		commands.put("info", info);
		commands.put("정보", info);
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
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		List<String> result = new ArrayList<>();
		if (args.length == 1) result.addAll(List.of("help", "도움", "sell", "판매", "buy", "구매", "list", "목록"));
		if (args.length == 2) {
			var str = args[0].toLowerCase();
			if (str.equalsIgnoreCase("sell") || str.equals("판매")) {
				result.addAll(sm.getStocks().stream().map(Stock::getName).toList());
			}
		}
		return result;
	}
}
