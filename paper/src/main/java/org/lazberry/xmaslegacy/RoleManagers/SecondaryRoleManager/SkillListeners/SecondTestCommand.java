package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.SkillListeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.lazberry.xmaslegacy.Annotation.Commands;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.*;

@TestOnly
@Commands(command = "second")
public class SecondTestCommand implements CommandExecutor {

    public SecondTestCommand() {
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		@NotNull SecondRoleManager srm = SecondRoleManager.INSTANCE;
		var berserker = srm.getRoleInstance(BERSERKER);
		var defender = srm.getRoleInstance(DEFENDER);
		var guardian = srm.getRoleInstance(GUARDIAN);
		var fighter = srm.getRoleInstance(FIGHTER);
		var sniper = srm.getRoleInstance(SNIPER);
		var ranger = srm.getRoleInstance(RANGER);
		if (args.length == 1) {
			switch (args[0]) {
				case "berserker" -> {
					p.getInventory().addItem(berserker.roleWeapon());
					p.getInventory().addItem(berserker.roleArmor());
					p.getInventory().addItem(berserker.targetEmblem());
					p.getInventory().addItem(berserker.rangeEmblem());
				}
				case "defender" -> {
					p.getInventory().addItem(defender.roleWeapon());
					p.getInventory().addItem(defender.roleArmor());
					p.getInventory().addItem(defender.targetEmblem());
					p.getInventory().addItem(defender.rangeEmblem());
				}
				case "guardian" -> {
					p.getInventory().addItem(guardian.roleWeapon());
					p.getInventory().addItem(guardian.roleArmor());
					p.getInventory().addItem(guardian.targetEmblem());
					p.getInventory().addItem(guardian.rangeEmblem());
				}
				case "fighter" -> {
					p.getInventory().addItem(fighter.roleWeapon());
					p.getInventory().addItem(fighter.roleArmor());
					p.getInventory().addItem(fighter.targetEmblem());
					p.getInventory().addItem(fighter.rangeEmblem());
				}
				case "sniper" -> {
					p.getInventory().addItem(sniper.roleWeapon());
					p.getInventory().addItem(sniper.roleArmor());
					p.getInventory().addItem(sniper.targetEmblem());
					p.getInventory().addItem(sniper.rangeEmblem());
				}
				case "ranger" -> {
					p.getInventory().addItem(ranger.roleWeapon());
					p.getInventory().addItem(ranger.roleArmor());
					p.getInventory().addItem(ranger.targetEmblem());
					p.getInventory().addItem(ranger.rangeEmblem());
				}
			}
		}
		return true;
	}
}
