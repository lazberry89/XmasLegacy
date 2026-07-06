package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Mage;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
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

import java.util.ArrayList;
import java.util.List;

@Skill(type = PlayerSkills.GRAVITY)
public class Gravity implements Skills<Mage.Container>, UsingEnergy {
	private final @NotNull SkillEffectManager sem;

	public Gravity() {
		this.sem = SkillEffectManager.INSTANCE;
	}

	@Override
	public boolean execute(@NotNull Player caster, Mage.@NotNull Container container) {
		if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;
		final @NotNull Location center = caster.getEyeLocation()
				.add(caster.getLocation().getDirection().multiply(container.second_skill_distance()));

		if (center.getBlock().getType().isSolid()) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 해당 위치에 스킬을 사용할 수 없습니다!"));
			return false;
		}
		List<BlockDisplay> cores = new ArrayList<>();
		for (int i = 0; i < 3; i++)
			cores.add(spawnDisplay(center, container));

		particleEffect(center);

		caster.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.7f);
		startEffectTask(caster, cores, center, container);
		return true;
	}

	private void particleEffect(@NotNull Location center) {
		Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 1.0f);
		sem.drawCircularLine(center, Particle.DUST, 3, true, 120, dust);
		sem.drawCircularLine(center.clone().add(0, -0.5, 0), Particle.DUST, 2.5, true, 120, dust);
		sem.drawCircularLine(center.clone().add(0, 0.5, 0), Particle.DUST, 2.5, true, 120, dust);
	}

	private void startEffectTask(@NotNull Player caster, @NotNull List<BlockDisplay> cores, @NotNull Location center, @NotNull Mage.Container container) {
		new BukkitRunnable() {
			int ticks = 0;
			@Override
			public void run() {
				if (ticks > container.second_skill_max_ticks() || !caster.isOnline()) {
					cores.forEach(Entity::remove);
					this.cancel();
					return;
				}

				for (int i = 0; i < cores.size(); i++) {
					BlockDisplay bd = cores.get(i);
					Transformation trans = bd.getTransformation();
					calculateAngle(trans, ticks, i);
					bd.setTransformation(trans);
					bd.setInterpolationDuration(1);
				}
				playEffect(caster, center, container, ticks);
				ticks++;
			}
		}.runTaskTimer(container.plugin(), 0, 1);
	}

	private void calculateAngle(@NotNull Transformation trans, int ticks, int count) {
		float angle = (float) Math.toRadians(ticks * 15);
		if (count == 0) trans.getLeftRotation().set(new Quaternionf().rotationXYZ(angle, angle * 0.5f, 0));
		else if (count == 1) trans.getLeftRotation().set(new Quaternionf().rotationXYZ(0, angle, angle * 0.5f));
		else trans.getLeftRotation().set(new Quaternionf().rotationXYZ(angle * 0.5f, 0, angle));
	}

	private @NotNull BlockDisplay spawnDisplay(@NotNull Location center, @NotNull Mage.@NotNull Container container) {
		return center.getWorld().spawn(center, BlockDisplay.class, display -> {
			display.setBlock(container.second_skill_display_material().createBlockData());
			display.setBrightness(new Display.Brightness(15, 15));
			Transformation trans = display.getTransformation();
			trans.getScale().set(1.2f, 1.2f, 1.2f);
			trans.getTranslation().set(-0.6f, -0.6f, -0.6f);
			display.setTransformation(trans);
			display.setInterpolationDuration(1);
			display.setInterpolationDelay(0);
		});
	}

	private void playEffect(@NotNull Player caster, @NotNull Location center, @NotNull Mage.@NotNull Container container, int ticks) {
		center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 10, 0.5, 0.5, 0.5, 0.1);
		for (Entity e : center.getWorld().getNearbyEntities(center, 6.0, 6.0, 6.0)) {

			if (e instanceof LivingEntity le && !e.equals(caster)) {
				if (ticks % 10 == 0) le.damage(1.0, caster);

				Vector direction = center.toVector().subtract(le.getLocation().toVector());

				double distance = direction.length();

				if (distance > container.second_skill_pull_threshold()) {
					direction.normalize();
					le.setVelocity(direction.multiply(container.second_skill_pull_strength()).setY(0.1));
				} else {
					le.setVelocity(new Vector(0, 0.02, 0));
				}
			}
		}
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.GRAVITY;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.MAGE;
	}
}
