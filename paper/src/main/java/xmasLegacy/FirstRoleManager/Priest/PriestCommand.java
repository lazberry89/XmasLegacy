package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class PriestCommand implements CommandExecutor, TabCompleter {
	private final ConductableItems CDI;

	public PriestCommand(ConductableItems CDI) {
		this.CDI = CDI;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 1) {
			switch (args[0]) {
				case "heal" -> p.getInventory().addItem(CDI.HealerPotion());
				case "dragon" -> p.getInventory().addItem(CDI.DragonPotion());
				case "protection" -> p.getInventory().addItem(CDI.ProtectionPotion());
				case "spear" -> p.getInventory().addItem(CDI.SpearPotion());
				case "saver" -> p.getInventory().addItem(CDI.DeathSave());
			}
		}
		return false;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		return List.of("heal", "dragon", "protection", "spear", "saver");
	}
}
