package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Fighter;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.StunUtils;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.FIGHTER;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.COUNTER;

@Skill(type = PlayerSkills.COUNTER)
public class Counter implements Skills<Fighter.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Fighter.@NotNull Container container) {
		if (!(caster.getTargetEntity(2, false) instanceof LivingEntity target)) {
			InfoUtils.error(caster, "유효한 타겟이 없습니다.");
			return false;
		}
		if (!consumeEnergy(caster, 3)) return false;
		var sem = SkillEffectManager.INSTANCE;

		caster.setCollidable(false);
		sem.hideEntity(caster);
		Bukkit.getScheduler().runTaskLater(container.plugin(), () -> {caster.setCollidable(true); sem.showEntity(caster);}, 5L);

		caster.getWorld().spawnParticle(Particle.ASH, caster.getLocation(), 10, 0.5, 0.5, 0.5, 0.01);
		caster.getWorld().playSound(caster, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.0f);
		caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);

		StunUtils.stun(target.getUniqueId(), 30L, "반격");

		Vector vector = caster.getLocation().getDirection();
		caster.setVelocity(vector.multiply(3.0).setY(Math.min(vector.getY(), 1.5)));
		Location startLoc = caster.getLocation().add(0, 1, 0);
		Vector dir = vector.clone().normalize();

		Vector vector1;
		if (Math.abs(dir.getY()) > 0.9) {
			vector1 = new Vector(1, 0, 0);
		} else {
			vector1 = new Vector(-dir.getZ(), 0, dir.getX()).normalize();
		}

		double radius = 0.5;
		double spiralTightness = 3.5;
		vector1.multiply(radius);
		Particle.DustOptions option = new Particle.DustOptions(Color.BLUE, 1.1f);
		Particle.DustTransition trs = new Particle.DustTransition(Color.BLUE, Color.AQUA, 1.1f);

		for (double d = 0; d < 6.0; d += 0.15) {
			Location centerPoint = startLoc.clone().add(dir.clone().multiply(d));
			caster.getWorld().spawnParticle(Particle.DUST, centerPoint, 1, 0, 0, 0, 0, option);
			centerPoint.getNearbyEntitiesByType(LivingEntity.class, 0.5, 0.5)
					.stream()
					.filter(s -> !PartyManager.INSTANCE.isParty(caster.getUniqueId(), s.getUniqueId()))
					.forEach(s -> s.damage(2.0, caster));

			double radians = d * spiralTightness;
			Vector rotatedOffset = vector1.clone().rotateAroundAxis(dir, radians);

			Location spiralPoint = centerPoint.clone().add(rotatedOffset);

			caster.getWorld().spawnParticle(Particle.DUST, spiralPoint, 1, 0, 0, 0, 0, trs);
		}
		return true;
	}

	@Override
	public @NotNull SkillSet type() {
		return COUNTER;
	}

	@Override
	public @NotNull Role role() {
		return FIGHTER;
	}
}
