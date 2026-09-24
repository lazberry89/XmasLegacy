package org.lazberry.xmaslegacy.blueprint.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.blueprint.BluePrintManager;
import org.lazberry.xmaslegacy.blueprint.selection.BlueprintSelectionManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Commands(command = "blueprint")
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintCommand implements CommandExecutor, TabCompleter {
	private final Map<String, SubCommand> commands = new HashMap<>();
	private final BlueprintSelectionManager selectionManager;
	private final BluePrintManager manager;

	@Inject
	public BlueprintCommand(BlueprintSelectionManager selectionManager, BluePrintManager manager) {
		this.selectionManager = selectionManager;
		this.manager = manager;
		commands.put("create", new BlueprintCommandCreate(manager, selectionManager));
		commands.put("give", new BlueprintCommandGive(manager));
		commands.put("build", new BlueprintCommandBuild(manager));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 0) {
			if (p.isOp()) InventoryHelper.giveItemOrDrop(p, selectionManager.tool());
			else InfoUtils.error(p, "올바른 명령어가 아닙니다.");
			return true;
		}
		var sub = commands.get(args[0].toLowerCase());
		if (sub == null) {
			InfoUtils.error(p, "올바른 명령어가 아닙니다.");
			return true;
		}
		sub.execute(p, args);
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		return switch (args.length) {
			case 1 -> List.of("create", "give", "build");
			case 2 -> {
				String sub = args[0].toLowerCase();
				if (sub.equals("give") || sub.equals("build")) {
					var list = manager.getBluePrints();
					if (list.isEmpty()) yield List.of();

					String input = args[1].toLowerCase();
					yield list.stream()
							.map(BluePrint::getStructureName)
							.filter(name -> name.toLowerCase().startsWith(input))
							.toList();
				}
				yield List.of();
			}
			default -> null;
		};
	}
}
