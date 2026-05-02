package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PriestSystemShopCommand implements CommandExecutor {
	StockInterface shopInterface = new StockInterface();
	private final PriestShop PSP;

	public PriestSystemShopCommand(PriestShop PSP) {
		this.PSP = PSP;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) return true;
		if (args.length == 0) {
			p.openInventory(shopInterface.getInventory());
		} else {
			PSP.openShop(p, p);
		}
		return false;
	}
}
