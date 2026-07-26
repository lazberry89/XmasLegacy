package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Archer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.BACK_DASH)
@Registry.Exclude(type = ServerType.LOBBY)
public class BackDash implements Skills<Archer.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, @NotNull Archer.Container container) {
		if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;
		caster.setInvulnerable(true);
		caster.getWorld().createExplosion(caster.getLocation(), container.second_skill_explosion_power(), false, false);
		Vector vector = caster.getLocation().getDirection();
		caster.setVelocity(vector.multiply(container.second_skill_backdash_multiplier()).setY(container.second_skill_backdash_y()));
		Bukkit.getScheduler().runTaskLater(container.plugin(), () -> {
			if (caster.isValid()) {
				caster.setInvulnerable(false);
			}
		}, container.second_skill_invulnerable_duration());
		return true;
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.BACK_DASH;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.ARCHER;
	}
}
