package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Commands(command = "상점")
@Registry.Exclude(type = ServerType.LOBBY)
public class ShopCommand implements CommandExecutor {
	private final @NotNull PriceManager priceManager;

	@Inject
	public ShopCommand(@NotNull PriceManager priceManager) {
		this.priceManager = priceManager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return false;
		p.openInventory(priceManager.MerchantShop());
		p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
		return true;
	}
}
