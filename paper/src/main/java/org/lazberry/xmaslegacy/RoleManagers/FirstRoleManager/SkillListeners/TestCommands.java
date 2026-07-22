package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.SkillListeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.lazberry.xmaslegacy.ParseEnum;
import org.lazberry.xmaslegacy.RoleManagers.RoleManager;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.ArrayList;
import java.util.List;

@TestOnly
@Commands(command = "test")
@Registry.Exclude(type = ServerType.LOBBY)
public class TestCommands implements CommandExecutor {
	private final @NotNull RoleManager rm;

	@Inject
	public TestCommands(@NotNull RoleManager rm) {
		this.rm = rm;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
		if (!(commandSender instanceof Player p)) return true;
		if (!p.isOp()) return true;

		if (args.length == 2) {
			BasicRoles role = ParseEnum.of(BasicRoles.class).parseOrDefault(args[0], BasicRoles.WARRIOR);
			AbstractFirstRole inst = rm.getBasicInstance(role);
			if (args[0].equalsIgnoreCase("weapon")) {
				List<ItemStack> weapons = new ArrayList<>();
				weapons.add(inst.roleWeapon());
				weapons.add(inst.roleArmor());
				weapons.add(inst.TargetEmblem());
				weapons.add(inst.RangeEmblem());
				weapons.forEach(i -> p.getInventory().addItem(i));
			} else if (args[0].equalsIgnoreCase("book"))
				p.getInventory().addItem(inst.roleBook());
		}
		return false;
	}
}
