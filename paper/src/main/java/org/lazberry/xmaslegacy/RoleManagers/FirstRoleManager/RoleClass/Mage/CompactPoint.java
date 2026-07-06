package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Mage;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.COMPACT_POINT)
public class CompactPoint implements Skills<Mage.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Mage.@NotNull Container container) {
		if (!consumeEnergy(caster, container.first_skill_hunger_cost())) return false;
		Location startLoc = caster.getEyeLocation();
		Vector dir = startLoc.getDirection().normalize().multiply(container.first_skill_speed());

		ArmorStand orb = caster.getWorld().spawn(startLoc, ArmorStand.class, stand -> {
			stand.setVisible(false);
			stand.setMarker(true);
			stand.setGravity(false);
		});

		new BukkitRunnable() {
			int ticks = 0;
			@Override
			public void run() {
				if (ticks > container.first_skill_max_ticks() || !orb.isValid() || orb.getLocation().getBlock().isSolid()) {
					explode(orb.getLocation(), caster, container);
					orb.remove();
					this.cancel();
					return;
				}

				orb.teleport(orb.getLocation().add(dir));
				Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 1.0f);
				orb.getWorld().spawnParticle(Particle.DUST, orb.getLocation(), 15, 0.1, 0.1, 0.1, 0.02, dust);

				for (Entity e : orb.getNearbyEntities(1.0, 1.0, 1.0)) {
					if (e instanceof LivingEntity && !e.equals(caster)) {
						explode(orb.getLocation(), caster, container);
						orb.remove();
						this.cancel();
						return;
					}
				}
				ticks++;
			}
		}.runTaskTimer(container.plugin(), 0, 1);
		return true;
	}

	private void explode(Location loc, Player source, Mage.@NotNull Container container) {
		loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
		loc.getWorld().createExplosion(source, loc, (float) container.first_skill_explosion_power(), false, false);
		for (Entity e : loc.getWorld().getNearbyEntities(loc, container.first_skill_slow_range_x(), container.first_skill_slow_range_y(), container.first_skill_slow_range_z())) {
			if (e instanceof LivingEntity le && !e.equals(source)) {
				le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, container.first_skill_slow_duration(), container.first_skill_slow_amplifier(), true, false, false));
			}
		}
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.COMPACT_POINT;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.MAGE;
	}
}
