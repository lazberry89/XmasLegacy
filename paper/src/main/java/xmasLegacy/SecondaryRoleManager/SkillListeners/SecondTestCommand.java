package xmasLegacy.SecondaryRoleManager.SkillListeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import xmasLegacy.SecondaryRoleManager.Berserker;
import xmasLegacy.SecondaryRoleManager.Defender;
import xmasLegacy.SecondaryRoleManager.Guardian;

@TestOnly
public class SecondTestCommand implements CommandExecutor {
	private final Berserker berserker;
	private final Defender defender;
	private final Guardian guardian;

    public SecondTestCommand() {
		this.berserker = Berserker.getInstance();
		this.defender = Defender.getInstance();
		this.guardian = Guardian.getInstance();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 1) {
			switch (args[0]) {
				case "berserker" -> {
					p.getInventory().addItem(berserker.roleWeapon());
					p.getInventory().addItem(berserker.roleArmor());
					p.getInventory().addItem(berserker.RangeEmblem());
					p.getInventory().addItem(berserker.TargetEmblem());
				}
				case "defender" -> {
					p.getInventory().addItem(defender.roleWeapon());
					p.getInventory().addItem(defender.roleArmor());
					p.getInventory().addItem(defender.RangeEmblem());
					p.getInventory().addItem(defender.TargetEmblem());
				}
				case "guardian" -> {
					p.getInventory().addItem(guardian.roleWeapon());
					p.getInventory().addItem(guardian.roleArmor());
					p.getInventory().addItem(guardian.RangeEmblem());
					p.getInventory().addItem(guardian.TargetEmblem());
				}
			}
		}
		return true;
	}
}
