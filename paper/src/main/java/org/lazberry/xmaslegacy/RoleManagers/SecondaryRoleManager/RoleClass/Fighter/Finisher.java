package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Fighter;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.FIGHTER;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.FINISHER;

@Skill(type = PlayerSkills.FINISHER)
public class Finisher implements Skills<Fighter.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Fighter.@NotNull Container container) {
		if (!(caster.getTargetEntity(2, false) instanceof LivingEntity target)) {
			InfoUtils.error(caster, "유효한 타겟이 없습니다.");
			return false;
		}
		if (!consumeEnergy(caster, 3)) return false;
		Location centerLoc = caster.getLocation().clone();
		caster.getWorld().spawnParticle(Particle.EXPLOSION, centerLoc, 1, 0, 0, 0, 0);

		Vector meUp = new Vector(0.0f, 0.6f, 0.0f);
		Vector targetUp = new Vector(0.0f, 2.5f, 0.0f);
		caster.setVelocity(meUp);
		caster.swingMainHand();

		spawnExpandingShockwave(caster, centerLoc, container);

		caster.getWorld().playSound(centerLoc, Sound.ENTITY_BREEZE_JUMP, 1.5f, 0.5f);

		double damage = 12;

		UpperCutEffect(target.getLocation().clone().add(0, 1, 0), container);
		target.setVelocity(targetUp);

		Bukkit.getScheduler().runTaskLater(container.plugin(), () -> target.damage(damage, caster), 5L);
		return true;
	}

	private void spawnExpandingShockwave(@NotNull Player p, @NotNull Location center, @NotNull Fighter.@NotNull Container container) {
		new org.bukkit.scheduler.BukkitRunnable() {
			double radius = 0.5;
			final double maxRadius = 4.5;
			final double expansionSpeed = 0.8;
			final Particle.DustTransition dustOptions = new Particle.DustTransition(Color.RED, Color.BLACK, 1.2f);

			@Override
			public void run() {
				if (radius > maxRadius) {
					this.cancel();
					return;
				}

				int particleCount = (int) (radius * 12);

				for (int i = 0; i < particleCount; i++) {
					double angle = 2 * Math.PI * i / particleCount;
					double x = Math.cos(angle) * radius;
					double z = Math.sin(angle) * radius;

					Location particleLoc = center.clone().add(x, 0.1, z);

					p.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dustOptions);

					if (i % 3 == 0) {
						p.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0.1, 0, 0.05);
					}
				}
				radius += expansionSpeed;
			}
		}.runTaskTimer(container.plugin(), 0L, 1L);
	}

	private void UpperCutEffect(@NotNull Location loc, @NotNull Fighter.@NotNull Container container) {
		var oraxen = OraxenItems.getItemById("haki_wave");
		if (oraxen == null) {
			container.plugin().getSLF4JLogger().error("Could not find Model \"haki_wave\"");
			return;
		}
		ItemStack wave = oraxen.build();
		ItemDisplay display = loc.getWorld().spawn(loc, ItemDisplay.class, w -> {
			w.setItemStack(wave);
			w.setInterpolationDuration(4);
			w.setBrightness(new Display.Brightness(15, 15));
			Transformation trans = w.getTransformation();
			trans.getScale().set(1.0f, 1.0f, 1.0f);
			w.setTransformation(trans);
		});
		Bukkit.getScheduler().runTaskLater(container.plugin(), () -> {
			if (!display.isValid()) return;

			Transformation targetTrans = display.getTransformation();
			targetTrans.getScale().set(7.0f, 1.0f, 7.0f);
			display.setInterpolationDelay(0);
			display.setTransformation(targetTrans);
		}, 2L);

		Bukkit.getScheduler().runTaskLater(container.plugin(), display::remove, 7L);
	}

	@Override
	public @NotNull SkillSet type() {
		return FINISHER;
	}

	@Override
	public @NotNull Role role() {
		return FIGHTER;
	}
}
