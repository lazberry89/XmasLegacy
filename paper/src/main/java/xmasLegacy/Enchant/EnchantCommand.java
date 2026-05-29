package xmasLegacy.Enchant;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnchantCommand implements CommandExecutor {

	public EnchantCommand() {}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 0) {
			EnchantUserInterface eui = new EnchantUserInterface();
			p.openInventory(eui.getInventory());
		} else {
			p.getInventory().addItem(EnchantMaterial.PrismFractal());
		}
		return true;
	}
}
