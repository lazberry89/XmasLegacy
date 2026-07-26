package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Commands(command = "shop")
@Registry.Exclude(type = ServerType.LOBBY)
public class TempCommand implements CommandExecutor {
	private final PriceManager PCI;

	@Inject
	public TempCommand(PriceManager pci) {
		PCI = pci;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		if (!(commandSender instanceof Player p)) return false;
		p.openInventory(PCI.MerchantShop());
		return false;
	}
}
