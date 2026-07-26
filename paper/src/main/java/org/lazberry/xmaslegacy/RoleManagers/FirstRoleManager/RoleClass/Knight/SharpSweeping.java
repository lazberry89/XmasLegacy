package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Knight;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Skill(type = PlayerSkills.SHARP_SWEEPING)
@Registry.Exclude(type = ServerType.LOBBY)
public class SharpSweeping implements Skills<Knight.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Knight.@NotNull Container container) {
		if (!consumeEnergy(caster, container.first_skill_hunger_cost())) return false;
		Vector direction = caster.getLocation().getDirection().normalize();
		caster.setVelocity(direction.multiply(container.first_skill_speed()).setY(container.first_skill_y_velocity()));

		caster.setPose(Pose.SPIN_ATTACK);

		caster.playSound(caster.getLocation(), Sound.ITEM_TRIDENT_THROW, 1, 1);
		new BukkitRunnable() {
			int ticks = 0;
			final int maxTicks = container.first_skill_max_ticks();
			final Set<UUID> hitEntities = new HashSet<>();

			@Override
			public void run() {
				if (ticks >= maxTicks || !caster.isOnline()) {
					caster.setPose(Pose.STANDING);
					this.cancel();
					return;
				}
				caster.spawnParticle(Particle.SWEEP_ATTACK, caster.getLocation().add(0, 1, 0), 1);
				caster.setPose(Pose.SPIN_ATTACK);
				Vector currentV = caster.getVelocity();
				caster.setVelocity(currentV.add(new Vector(0, container.first_skill_tick_y_add(), 0)));

				for (Entity entity : caster.getNearbyEntities(container.first_skill_range(), container.first_skill_range(), container.first_skill_range())) {
					if (entity instanceof LivingEntity target && !entity.equals(caster)) {

						if (hitEntities.contains(target.getUniqueId())) continue;

						target.damage(container.first_skill_damage(), caster);
						target.getWorld().playSound(target.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.5F);
						target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, container.first_skill_slow_duration(), container.first_skill_slow_amplifier(), false, false, true));

						Vector push = direction.clone().multiply(container.first_skill_knockback_multiplier()).setY(container.first_skill_knockback_y());
						target.setVelocity(push);

						hitEntities.add(target.getUniqueId());
					}
				}

				ticks++;
			}
		}.runTaskTimer(XmasLegacy.getInstance(), 0L, 1L);
		return true;
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.SHARP_SWEEPING;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.KNIGHT;
	}
}
