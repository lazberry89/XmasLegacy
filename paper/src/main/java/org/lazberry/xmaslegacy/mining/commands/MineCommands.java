package org.lazberry.xmaslegacy.mining.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.mining.logics.MineCreateManager;
import org.lazberry.xmaslegacy.mining.logics.MineManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.List;
import java.util.UUID;

@Commands(command = "mine")
@Registry.Include(type = ServerType.MAIN)
public class MineCommands implements CommandExecutor, TabCompleter {
	private final MineCreateManager mcm;
	private final MineManager mm;

	@Inject
	public MineCommands(MineCreateManager mcm, MineManager mm) {
		this.mcm = mcm;
		this.mm = mm;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (!p.isOp()) {
			InfoUtils.error(p, "You don't have permission to use this command!");
			return true;
		}
		UUID uuid = p.getUniqueId();
		if (args.length == 1 && args[0].equalsIgnoreCase("create")) {
			Location loc1 = mcm.getFirstSelection(uuid);
			Location loc2 = mcm.getSecondSelection(uuid);
			if (loc1 == null || loc2 == null) {
				InfoUtils.error(p, "위치를 설정 후 사용해주세요.");
				return true;
			}
		}
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		return List.of();
	}
}
