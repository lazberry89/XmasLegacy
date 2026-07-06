package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Rogue;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.DAGGER_RUSH)
public class DaggerRush implements Skills<Rogue.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Rogue.@NotNull Container container) {
		Entity target = caster.getTargetEntity(container.first_skill_range(), false);
		if (target == null) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 타겟이 없습니다!"));
			return false;
		}
		if (!(target instanceof LivingEntity le)) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 유효한 타겟이 아닙니다!"));
			return false;
		}
		if (!consumeEnergy(caster, container.first_skill_hunger_cost())) return false;

		Vector vector = caster.getLocation().getDirection().normalize();
		caster.setVelocity(vector.multiply(container.first_skill_speed()).setY(container.first_skill_y_velocity()));
		SkillEffectManager.INSTANCE.followParticle(caster, Particle.DUST, 0.5, new Particle.DustOptions(Color.GRAY, 1.5f));
		dashScheduler(caster, le, container);
		return true;
	}

	private void dashScheduler(@NotNull Player caster, @NotNull LivingEntity le, Rogue.@NotNull Container container) {
		new BukkitRunnable() {
			int timeout = 0;

			@Override
			public void run() {
				timeout++;

				if (timeout > container.first_skill_timeout_ticks()
						|| !caster.isOnline()
						|| le.isDead()) {
					this.cancel();
					return;
				}
				if (!caster.getWorld().equals(le.getWorld())) {
					this.cancel();
					return;
				}
				if (caster.getLocation().distanceSquared(le.getLocation()) <= Math.pow(container.first_skill_hit_range(), 2)) {
					useDaggerRush(caster, le, container);
					this.cancel();
				}
			}
		}.runTaskTimer(container.plugin(), 0L, 1L);
	}

	private void useDaggerRush(@NotNull Player player, @NotNull LivingEntity target, Rogue.@NotNull Container container) {
		new BukkitRunnable() {
			int count = 0;

			@Override
			public void run() {
				if (count >= container.dagger_rush_hits() || !target.isValid() || target.isDead()) {
					this.cancel();
					return;
				}
				target.setNoDamageTicks(0);

				target.damage(container.dagger_rush_damage(), player);

				target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 1);
				target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);

				count++;
			}
		}.runTaskTimer(container.plugin(), 0L, 2L);
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.DAGGER_RUSH;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.ROGUE;
	}
}
