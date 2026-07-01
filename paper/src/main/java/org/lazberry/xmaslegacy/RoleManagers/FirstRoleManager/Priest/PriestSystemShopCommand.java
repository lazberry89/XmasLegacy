package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Priest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Commands;

@Commands(command = "system")
public class PriestSystemShopCommand implements CommandExecutor {
	StockInterface shopInterface = new StockInterface();
	private final PriestShopManager PSM;

	public PriestSystemShopCommand() {
		this.PSM = PriestShopManager.INSTANCE;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) return true;
		if (args.length == 0) {
			p.openInventory(shopInterface.getInventory());
		} else {
            PriestShop priestAShop = PSM.get(p.getUniqueId());
			if (priestAShop == null || !priestAShop.isShopEnabled()) {
				p.sendMessage("상점이 열려있지 않습니다!");
				return true;
			}
			priestAShop.openShop(p);
		}
		return false;
	}
}
