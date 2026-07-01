package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Skills.Crafter;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

public class TempBuff implements Skills<Crafter.Container> {

	@Override
	public void execute(@NotNull Player caster, Crafter.@NotNull Container container) {

	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.TEMP_BUFF;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.CRAFTER;
	}
}
