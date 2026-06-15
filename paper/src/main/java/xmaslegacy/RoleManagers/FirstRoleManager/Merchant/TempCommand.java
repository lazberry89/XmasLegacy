package xmaslegacy.RoleManagers.FirstRoleManager.Merchant;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.Annotation.Commands;

@Commands(command = "shop")
public class TempCommand implements CommandExecutor {
	private final PriceManager PCI;

	public TempCommand() {
		this.PCI = PriceManager.INSTANCE;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		if (!(commandSender instanceof Player p)) return false;
		p.openInventory(PCI.MerchantShop());
		return false;
	}
}
