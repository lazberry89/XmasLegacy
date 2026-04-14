package org.lazberry.xmasLegacy.FirstRoleManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmasLegacy.SkillEffectManager;
import org.lazberry.xmasLegacy.XmasLegacy;

public class TestCommands implements CommandExecutor {
	private final SkillEffectManager SEM;
	private final XmasLegacy plugin;

	public TestCommands(SkillEffectManager SEM, XmasLegacy plugin) {
		this.SEM = SEM;
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) return true;

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("weapon")) {
				switch (args[1]) {
					case "sword" -> {
						AbstractFirstRole k = new Knight(5, 5, SEM, plugin);
						p.getInventory().addItem(k.roleWeapon());
						p.getInventory().addItem(k.roleArmor());
					}
					case "dagger" -> {
						AbstractFirstRole r = new Rogue(5, 5, SEM, plugin);
						p.getInventory().addItem(r.roleWeapon());
						p.getInventory().addItem(r.roleArmor());
					}
				}
			}
		}
		return false;
	}
}
