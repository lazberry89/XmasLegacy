package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Priest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;

@Commands(command = "system")
public class PriestSystemShopCommand implements CommandExecutor {
	StockInterface shopInterface = new StockInterface();
	private final PriestShopManager psm;

	@Inject
	public PriestSystemShopCommand(@NotNull PriestShopManager psm) {
		this.psm = psm;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) return true;
		if (args.length == 0) {
			p.openInventory(shopInterface.getInventory());
		} else {
            PriestShop priestAShop = psm.get(p.getUniqueId());
			if (priestAShop == null || !priestAShop.isShopEnabled()) {
				p.sendMessage("상점이 열려있지 않습니다!");
				return true;
			}
			priestAShop.openShop(p);
		}
		return false;
	}
}
