package org.lazberry.xmaslegacy.Cosmetics;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.lazberry.xmaslegacy.Annotation.Commands;

@TestOnly
@Commands(command = "head")
public class TestHeadCommand implements CommandExecutor {
	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		ItemStack head = p.getInventory().getItemInMainHand();
		if (head.getType().isAir()) return false;
		p.getInventory().setHelmet(head);
		return true;
	}
}
