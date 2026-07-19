package org.lazberry.xmaslegacy.Enchant;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;

@Commands(command = "강화")
public class EnchantCommand implements CommandExecutor {
	private final @NotNull EnchantManager ecm;

	@Inject
	public EnchantCommand(@NotNull EnchantManager ecm) {
		this.ecm = ecm;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 0) {
			EnchantUserInterface eui = new EnchantUserInterface(ecm);
			p.openInventory(eui.getInventory());
		} else {
			p.getInventory().addItem(EnchantMaterial.PrismFractal());
		}
		return true;
	}
}
