package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.SkillListeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.RoleManagers.RoleManager;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.ParseEnum;

@TestOnly
@Commands(command = "second")
public class SecondTestCommand implements CommandExecutor {

    public SecondTestCommand() {
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 0) return true;
		@NotNull RoleManager srm = RoleManager.INSTANCE;

		SecondaryRoles sr = ParseEnum.of(SecondaryRoles.class).parse(args[0]);
		if (sr == null) return true;
		AbstractSecondRole asr = srm.getRoleInstance(sr);
		p.getInventory().addItem(asr.roleWeapon());
		p.getInventory().addItem(asr.roleArmor());
		p.getInventory().addItem(asr.targetEmblem());
		p.getInventory().addItem(asr.rangeEmblem());
		return true;
	}
}
