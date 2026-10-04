package org.lazberry.xmaslegacy.enchant;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Commands(command = "강화")
@Registry.Exclude(type = ServerType.LOBBY)
public class EnchantCommand implements CommandExecutor {
	private final @NotNull EnchantManager ecm;
	private final EnchantifyEffectManager effectManager;

	@Inject
	public EnchantCommand(@NotNull EnchantManager ecm, EnchantifyEffectManager effectManager) {
		this.ecm = ecm;
		this.effectManager = effectManager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 0) {
			EnchantUserInterface eui = new EnchantUserInterface(ecm);
			p.openInventory(eui.getInventory());
		} else if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
			var item = p.getInventory().getItemInMainHand();
			if (item.getType().isAir()) return true;
			ecm.setEnchantable(item);
		} else if (args.length == 1 && args[0].equalsIgnoreCase("test")) {
			effectManager.playEnchantifyEffect(p, p.getLocation());
		} else {
			if (p.isOp()) p.getInventory().addItem(EnchantMaterial.PrismFractal());
		}
		return true;
	}
}
