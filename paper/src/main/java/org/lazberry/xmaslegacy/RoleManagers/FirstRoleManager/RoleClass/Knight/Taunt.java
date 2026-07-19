package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Knight;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.TAUNT)
public class Taunt implements Skills<Knight.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Knight.@NotNull Container container) {
		if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;
		caster.getWorld().spawnParticle(Particle.FLAME, caster.getLocation(), 30, 10, 10, 10, 0.01);
		caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.6f);

		for (Entity entity : caster.getWorld().getNearbyEntities(caster.getLocation(), container.second_skill_range(), container.second_skill_range(), container.second_skill_range())) {
			if (entity instanceof LivingEntity e && !caster.equals(e)) {
				if (e instanceof Mob mob) {
					mob.setTarget(caster);
					mob.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, mob.getLocation(), 10, 1.5, 1.5, 1.5, 0.1);
				}
				if (e instanceof Player target) SkillEffectManager.INSTANCE.knockbackEntity(caster, target, container.second_skill_knockback(), container.second_skill_knockback_y());
			}
		}
		return true;
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.TAUNT;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.KNIGHT;
	}
}
