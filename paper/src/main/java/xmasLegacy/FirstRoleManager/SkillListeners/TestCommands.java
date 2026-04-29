package xmasLegacy.FirstRoleManager.SkillListeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Party.PartyManager;
import xmasLegacy.FirstRoleManager.*;
import xmasLegacy.FirstRoleManager.Farmer.Farmer;
import xmasLegacy.FirstRoleManager.Gatherer.Gatherer;
import xmasLegacy.FirstRoleManager.Miner.Miner;
import xmasLegacy.FirstRoleManager.Priest.Priest;
import xmasLegacy.Region.RegionManager;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.XmasLegacy;

public class TestCommands implements CommandExecutor {
	private final SkillEffectManager SEM;
	private PartyManager PM;
	private final RegionManager RM;
	private final XmasLegacy plugin;

	public TestCommands(SkillEffectManager SEM, RegionManager RM, XmasLegacy plugin) {
		this.SEM = SEM;
		this.RM = RM;
		this.plugin = plugin;
	}

	public void setPM(PartyManager PM) {
		this.PM = PM;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) return true;

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("weapon")) {
				switch (args[1]) {
					case "knight" -> {
						AbstractFirstRole k = new Knight(SEM, plugin);
						p.getInventory().addItem(k.roleWeapon());
						p.getInventory().addItem(k.roleArmor());
					}
					case "rogue" -> {
						AbstractFirstRole r = new Rogue(5, 5, SEM, plugin);
						p.getInventory().addItem(r.roleWeapon());
						p.getInventory().addItem(r.roleArmor());
					}
					case "archer" -> {
						AbstractFirstRole a = new Archer(5, 5, plugin);
						p.getInventory().addItem(a.roleWeapon());
						p.getInventory().addItem(a.roleArmor());
					}
					case "warrior" -> {
						AbstractFirstRole w = new Warrior(4, 4, plugin);
						p.getInventory().addItem(w.roleWeapon());
						p.getInventory().addItem(w.roleArmor());
					}
                    case "mage" -> {
                        AbstractFirstRole m = new Mage(4, 4, plugin, SEM);
                        p.getInventory().addItem(m.roleWeapon());
                        p.getInventory().addItem(m.roleArmor());
                    }
					case "priest" -> {
						AbstractFirstRole pr = new Priest(4, 4, PM, SEM, plugin);
						p.getInventory().addItem(pr.roleWeapon());
						p.getInventory().addItem(pr.roleArmor());
					}
					case "farmer" -> {
						AbstractFirstRole f = new Farmer(4, 4, plugin, RM);
						p.getInventory().addItem(f.roleWeapon());
						p.getInventory().addItem(f.roleArmor());
					}
					case "miner" -> {
						AbstractFirstRole mi = new Miner(4, 4, plugin);
						p.getInventory().addItem(mi.roleWeapon());
						p.getInventory().addItem(mi.roleArmor());
					}
					case "gatherer" -> {
						AbstractFirstRole g = new Gatherer(4, 4, plugin);
						p.getInventory().addItem(g.roleWeapon());
						p.getInventory().addItem(g.roleArmor());
					}
				}
			}
		}
		return false;
	}
}
