package org.lazberry.xmaslegacy.shop.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.shop.goods.Goods;
import org.lazberry.xmaslegacy.shop.showcase.ShowCaseManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Commands(command = "goods")
@Registry.Exclude(type = ServerType.LOBBY)
public class GoodsCommand implements CommandExecutor, TabCompleter {
	private final Map<String, SubCommand> commands = new HashMap<>();
	private final ShowCaseManager showcaseManager;

	@Inject
	public GoodsCommand(ShowCaseManager showcaseManager) {
		this.showcaseManager = showcaseManager;
		this.commands.put("create", new GoodsCommandCreate(showcaseManager));
		this.commands.put("remove", new GoodsCommandRemove(showcaseManager));
		this.commands.put("showcase", new GoodsCommandShowcase(showcaseManager));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player player)) return true;
		if (args.length == 0) {
			InfoUtils.error(player, "유효한 명령어가 아닙니다.");
			return true;
		}
		var subCommand = commands.get(args[0].toLowerCase());
		if (subCommand == null) {
			InfoUtils.error(player, "유효한 명령어가 아닙니다.");
			return true;
		}
		subCommand.execute(player, args);
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (args.length == 1) return List.of("create", "remove", "showcase");
		if (args.length == 2 &&
				(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("showcase"))) {
			return showcaseManager.getGoods().stream()
					.map(Goods::getName)
					.toList();
		}
		if (args.length == 3 && args[0].equalsIgnoreCase("showcase")) {
			return List.of("create", "remove");
		}
		return List.of();
	}
}
