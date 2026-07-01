package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Skills.Archer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

public class BackDash implements Skills {

	@Override
	public void execute(@NotNull Player caster, @NotNull RoleContainer container) {
		if (!(container instanceof Archer.Container cnr)) {
			throw new IllegalArgumentException("아처 스킬에는 Archer.Container가 필요합니다.");
		}
		caster.setInvulnerable(true);
		caster.getWorld().createExplosion(caster.getLocation(), cnr.second_skill_explosion_power(), false, false);
		Vector vector = caster.getLocation().getDirection();
		caster.setVelocity(vector.multiply(cnr.second_skill_backdash_multiplier()).setY(cnr.second_skill_backdash_y()));
		Bukkit.getScheduler().runTaskLater(cnr.plugin(), () -> {
			if (caster.isValid()) {
				caster.setInvulnerable(false);
			}
		}, cnr.second_skill_invulnerable_duration());
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
