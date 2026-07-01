package org.lazberry.xmaslegacy.RoleManagers;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.SkillSet;

public interface Skills<C extends RoleContainer> {
	void execute(@NotNull Player caster, @NotNull C container);
	@NotNull SkillSet type();
	@NotNull Role role();
}
