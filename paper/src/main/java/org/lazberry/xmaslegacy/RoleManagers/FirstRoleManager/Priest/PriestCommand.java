package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Priest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Annotation.Commands;

import java.util.List;

@Commands(command = "potion")
public class PriestCommand implements CommandExecutor, TabCompleter {

	public PriestCommand() {}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 1) {
			switch (args[0]) {
				case "heal" -> p.getInventory().addItem(ConductableItems.HealerPotion());
				case "dragon" -> p.getInventory().addItem(ConductableItems.DragonPotion());
				case "protection" -> p.getInventory().addItem(ConductableItems.ProtectionPotion());
				case "spear" -> p.getInventory().addItem(ConductableItems.SpearPotion());
				case "saver" -> p.getInventory().addItem(ConductableItems.DeathSave());
			}
		}
		return false;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		return List.of("heal", "dragon", "protection", "spear", "saver");
	}
}
