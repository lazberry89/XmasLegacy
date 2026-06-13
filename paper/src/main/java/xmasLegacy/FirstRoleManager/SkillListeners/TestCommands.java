package xmasLegacy.FirstRoleManager.SkillListeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.Commands;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.FirstRoleManager.FirstRoleManager;

@TestOnly
@Commands(command = "test")
public class TestCommands implements CommandExecutor {
	private final FirstRoleManager frm;

	public TestCommands() {
		this.frm = FirstRoleManager.INSTANCE;
	}


	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) return true;
		AbstractFirstRole k = frm.getRoleInstance(Roles.KNIGHT);
		AbstractFirstRole r = frm.getRoleInstance(Roles.ROGUE);
		AbstractFirstRole a = frm.getRoleInstance(Roles.ARCHER);
		AbstractFirstRole w = frm.getRoleInstance(Roles.WARRIOR);
		AbstractFirstRole m = frm.getRoleInstance(Roles.MAGE);
		AbstractFirstRole pr = frm.getRoleInstance(Roles.PRIEST);
		AbstractFirstRole f = frm.getRoleInstance(Roles.FARMER);
		AbstractFirstRole mi = frm.getRoleInstance(Roles.MINER);
		AbstractFirstRole g = frm.getRoleInstance(Roles.GATHERER);
		AbstractFirstRole mc = frm.getRoleInstance(Roles.MERCHANT);

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("weapon")) {
				switch (args[1]) {
					case "knight" -> {
						p.getInventory().addItem(k.roleWeapon());
						p.getInventory().addItem(k.roleArmor());
					}
					case "rogue" -> {
						p.getInventory().addItem(r.roleWeapon());
						p.getInventory().addItem(r.roleArmor());
					}
					case "archer" -> {
						p.getInventory().addItem(a.roleWeapon());
						p.getInventory().addItem(a.roleArmor());
					}
					case "warrior" -> {
						p.getInventory().addItem(w.roleWeapon());
						p.getInventory().addItem(w.roleArmor());
					}
                    case "mage" -> {
                        p.getInventory().addItem(m.roleWeapon());
                        p.getInventory().addItem(m.roleArmor());
                    }
					case "priest" -> {
						p.getInventory().addItem(pr.roleWeapon());
						p.getInventory().addItem(pr.roleArmor());
					}
					case "farmer" -> {
						p.getInventory().addItem(f.roleWeapon());
						p.getInventory().addItem(f.roleArmor());
					}
					case "miner" -> {
						p.getInventory().addItem(mi.roleWeapon());
						p.getInventory().addItem(mi.roleArmor());
					}
					case "gatherer" -> {
						p.getInventory().addItem(g.roleWeapon());
						p.getInventory().addItem(g.roleArmor());
					}
					case "merchant" -> {
						p.getInventory().addItem(mc.roleWeapon());
						p.getInventory().addItem(mc.roleArmor());
					}
				}
			} else if (args[0].equalsIgnoreCase("book")) {
				switch (args[1]) {
					case "knight" -> p.getInventory().addItem(k.roleBook());
					case "rogue" -> p.getInventory().addItem(r.roleBook());
					case "archer" -> p.getInventory().addItem(a.roleBook());
					case "warrior" -> p.getInventory().addItem(w.roleBook());
					case "mage" -> p.getInventory().addItem(m.roleBook());
					case "priest" -> p.getInventory().addItem(pr.roleBook());
					case "farmer" -> p.getInventory().addItem(f.roleBook());
					case "miner" -> p.getInventory().addItem(mi.roleBook());
					case "gatherer" -> p.getInventory().addItem(g.roleBook());
					case "merchant" -> p.getInventory().addItem(mc.roleBook());
				}
			}
		}
		return false;
	}
}
