package org.lazberry.xmaslegacy.role.commands.shop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.role.shop.role.RoleShopManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.List;

@Commands(command = "shop")
@Registry.Exclude(type = ServerType.LOBBY)
public class ShopCommand implements CommandExecutor, TabCompleter {
	private final RoleShopManager roleShopManager;

	@Inject
	public ShopCommand(RoleShopManager roleShopManager) {
		this.roleShopManager = roleShopManager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (args.length == 1) {
			String shopName = args[0].toLowerCase();
			Inventory inv = switch (shopName) {
				case "farmer" -> roleShopManager.farmer().getSelectionGui().getInventory();
				case "fisherman" -> roleShopManager.fisherman().getFishSellingShop().getInventory();
				case "miner" -> roleShopManager.miner().getMinerSellingShop().getInventory();
				case "hunter" -> roleShopManager.hunter().getSellingShop().getInventory();
				default -> null;
			};
			if (inv != null) p.openInventory(inv);
			else InfoUtils.error(p, "유효하지 상점이 아닙니다.");
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		return List.of();
	}
}
