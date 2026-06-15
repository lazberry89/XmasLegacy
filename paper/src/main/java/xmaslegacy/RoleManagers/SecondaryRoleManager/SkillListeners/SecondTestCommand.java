package xmaslegacy.RoleManagers.SecondaryRoleManager.SkillListeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.RoleManagers.SecondaryRoleManager.*;
import xmaslegacy.RoleManagers.SecondaryRoleManager.Sniper.Sniper;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.*;

@TestOnly
@Commands(command = "second")
public class SecondTestCommand implements CommandExecutor {
	private final Berserker berserker;
	private final Defender defender;
	private final Guardian guardian;
	private final Fighter fighter;
	private final Sniper sniper;

    public SecondTestCommand() {
	    @NotNull SecondRoleManager srm = SecondRoleManager.INSTANCE;
		this.berserker = srm.getRoleInstance(BERSERKER);
		this.defender = srm.getRoleInstance(DEFENDER);
		this.guardian = srm.getRoleInstance(GUARDIAN);
		this.fighter = srm.getRoleInstance(FIGHTER);
		this.sniper = srm.getRoleInstance(SNIPER);;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 1) {
			switch (args[0]) {
				case "berserker" -> {
					p.getInventory().addItem(berserker.roleWeapon());
					p.getInventory().addItem(berserker.roleArmor());
					p.getInventory().addItem(berserker.TargetEmblem());
					p.getInventory().addItem(berserker.RangeEmblem());
				}
				case "defender" -> {
					p.getInventory().addItem(defender.roleWeapon());
					p.getInventory().addItem(defender.roleArmor());
					p.getInventory().addItem(defender.TargetEmblem());
					p.getInventory().addItem(defender.RangeEmblem());
				}
				case "guardian" -> {
					p.getInventory().addItem(guardian.roleWeapon());
					p.getInventory().addItem(guardian.roleArmor());
					p.getInventory().addItem(guardian.TargetEmblem());
					p.getInventory().addItem(guardian.RangeEmblem());
				}
				case "fighter" -> {
					p.getInventory().addItem(fighter.roleWeapon());
					p.getInventory().addItem(fighter.roleArmor());
					p.getInventory().addItem(fighter.TargetEmblem());
					p.getInventory().addItem(fighter.RangeEmblem());
				}
				case "sniper" -> {
					p.getInventory().addItem(sniper.roleWeapon());
					p.getInventory().addItem(sniper.roleArmor());
					p.getInventory().addItem(sniper.TargetEmblem());
					p.getInventory().addItem(sniper.RangeEmblem());
				}
			}
		}
		return true;
	}
}
