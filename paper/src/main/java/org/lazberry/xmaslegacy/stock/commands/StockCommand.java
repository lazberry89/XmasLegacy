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
import org.lazberry.xmaslegacy.stock.commands.admin.StockCommandAdmin;

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
		var admin = new StockCommandAdmin(sm);
		commands.put("sell", sell);
		commands.put("판매", sell);
		commands.put("list", list);
		commands.put("목록", list);
		commands.put("buy", buy);
		commands.put("구매", buy);
		commands.put("info", info);
		commands.put("정보", info);
		commands.put("admin", admin);
		commands.put("관리", admin);
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
		boolean en = command.getName().equalsIgnoreCase("stock");
		if (args.length == 1)
			if (en) result.addAll(List.of("help", "sell", "buy", "list", "admin", "info"));
			else result.addAll(List.of("판매", "구매", "리스트", "관리", "정보"));
		if (args.length == 2) {
			var str = args[0].toLowerCase();
			if (str.equalsIgnoreCase("buy") || str.equals("구매"))
				result.addAll(sm.getStocks().stream().map(Stock::getName).toList());
			if (str.equalsIgnoreCase("admin") || str.equals("관리"))
				if (en) result.addAll(List.of("add", "remove", "update"));
				else result.addAll(List.of("추가", "제거", "업데이트"));
		}
		if (args.length == 3) {
			var str = args[1].toLowerCase();
			if (str.equalsIgnoreCase("remove") || str.equals("제거"))
				result.addAll(sm.getStocks().stream().map(Stock::getName).toList());
		}
		return result;
	}
}
