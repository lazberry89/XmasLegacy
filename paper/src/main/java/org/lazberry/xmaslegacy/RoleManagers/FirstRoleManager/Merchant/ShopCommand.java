package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Merchant;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Commands;

@Commands(command = "상점")
public class ShopCommand implements CommandExecutor {

	public ShopCommand() {}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return false;
		p.openInventory(PriceManager.INSTANCE.MerchantShop());
		p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
		return true;
	}
}
