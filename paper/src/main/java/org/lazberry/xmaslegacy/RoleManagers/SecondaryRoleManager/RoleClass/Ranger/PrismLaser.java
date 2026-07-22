package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Ranger;

import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.*;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.RANGER;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.PRISM_LASER;

@Skill(type = PlayerSkills.PRISM_LASER)
public class PrismLaser implements Skills<Ranger.Container>, UsingEnergy {
	private final @NotNull PartyManager pm;

	@Inject
	public PrismLaser(@NotNull PartyManager pm) {
		this.pm = pm;
	}

	@Override
	public boolean execute(@NotNull Player caster, Ranger.@NotNull Container container) {
		Location startLoc = caster.getEyeLocation();
		Vector direction = startLoc.getDirection().normalize();
		double maxDistance = 60.0;

		RayTraceResult ray = caster.getWorld().rayTraceEntities(
				startLoc, direction, maxDistance, 0.5,
				entity -> entity instanceof LivingEntity
						&& !entity.equals(caster)
						&& !pm.isParty(caster.getUniqueId(), entity.getUniqueId())
		);

		if (ray == null || !(ray.getHitEntity() instanceof LivingEntity hitEntity)) {
			InfoUtils.error(caster, "타겟이 없습니다!");
			return false;
		}

		if (!consumeEnergy(caster, 4)) return false;

		double actualDistance = startLoc.distance(hitEntity.getEyeLocation()) + 1.5;

		if (actualDistance <= 15.0) executeShortRange(caster, startLoc, direction, actualDistance, hitEntity);
		else executeLongRange(caster, container, startLoc, direction, actualDistance);
		return true;
	}

	@Override
	public @NotNull SkillSet type() {
		return PRISM_LASER;
	}

	@Override
	public @NotNull Role role() {
		return RANGER;
	}

	private void executeShortRange(@NotNull Player caster, @NotNull Location start, @NotNull Vector dir, double distance, @NotNull LivingEntity hitEntity) {
		World world = start.getWorld();
		Particle.DustOptions thickDust = new Particle.DustOptions(Color.fromRGB(255, 230, 0), 1.5f);
		Particle.DustOptions waveDust = new Particle.DustOptions(Color.fromRGB(255, 255, 140), 1.0f);

		Vector axisX = (Math.abs(dir.getY()) > 0.9) ? new Vector(1, 0, 0) : new Vector(-dir.getZ(), 0, dir.getX()).normalize();
		Vector axisY = dir.clone().crossProduct(axisX).normalize();

		for (double d = 0; d < distance; d += 0.2) {
			Location point = start.clone().add(dir.clone().multiply(d));
			world.spawnParticle(Particle.DUST, point, 2, 0.02, 0.02, 0.02, 0, thickDust, true);
		}

		double waveRadius = 1.0;
		int circlePoints = 20;

		for (double d = 0.8; d < distance; d += 2.5) {
			Location centerPoint = start.clone().add(dir.clone().multiply(d));
			for (int i = 0; i < circlePoints; i++) {
				double angle = (2 * Math.PI * i) / circlePoints;
				Vector offset = axisX.clone().multiply(Math.cos(angle) * waveRadius)
						.add(axisY.clone().multiply(Math.sin(angle) * waveRadius));

				Location waveLoc = centerPoint.clone().add(offset);
				world.spawnParticle(Particle.DUST, waveLoc, 1, 0, 0, 0, 0, waveDust, true);
			}
		}

		world.playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.8f, 1.8f);

		hitEntity.damage(6.0, caster);
		hitEntity.setFireTicks(60);

		Vector knockback = dir.clone().setY(0.35).normalize().multiply(1.6);
		hitEntity.setVelocity(knockback);

		hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f, 1.2f);
	}

	private void executeLongRange(@NotNull Player caster, Ranger.@NotNull Container container, @NotNull Location start, @NotNull Vector dir, double distance) {
		World world = start.getWorld();
		List<BlockDisplay> displays = new ArrayList<>();

		world.playSound(start, Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 1.0f);
		world.playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.5f);

		Location spawnLoc = start.clone();
		spawnLoc.setDirection(dir);

		for (double d = 0; d < distance; d += 1.0) {
			Location blockPoint = spawnLoc.clone().add(dir.clone().multiply(d));
			BlockDisplay bd = world.spawn(blockPoint, BlockDisplay.class, display -> {
				display.setBlock(Bukkit.createBlockData(Material.YELLOW_STAINED_GLASS));

				Vector3f scale = new Vector3f(0.3f, 0.3f, 1.0f);
				// 중심을 잡기 위한 Translation (크기의 절반의 음수값)
				Vector3f translation = new Vector3f(-0.15f, -0.15f, -0.5f);

				display.setTransformation(new Transformation(translation, new Quaternionf(), scale, new Quaternionf()));
				display.setInterpolationDuration(2);
			});
			displays.add(bd);
		}

		new BukkitRunnable() {
			int ticksLived = 0;
			float angle = 0;
			final Set<UUID> hitEntities = new HashSet<>();

			@Override
			public void run() {
				if (ticksLived >= 10 || !caster.isOnline()) {
					displays.forEach(Entity::remove);
					this.cancel();
					return;
				}

				angle += 36.0f;
				Quaternionf leftRot = new Quaternionf().rotateAxis((float) Math.toRadians(angle), new Vector3f(0, 0, 1));
				Vector3f pivotOffset = new Vector3f(-0.15f, -0.15f, -0.5f);
				Vector3f newTranslation = new Vector3f();
				leftRot.transform(pivotOffset, newTranslation);

				for (BlockDisplay bd : displays) {
					Transformation current = bd.getTransformation();
					bd.setTransformation(new Transformation(newTranslation, leftRot, current.getScale(), current.getRightRotation()));
				}

				Vector axisX = (Math.abs(dir.getY()) > 0.9) ? new Vector(1, 0, 0) : new Vector(-dir.getZ(), 0, dir.getX()).normalize();
				Vector axisY = dir.clone().crossProduct(axisX).normalize();
				double radius = 0.6;
				Particle.DustOptions lineDust = new Particle.DustOptions(Color.fromRGB(255, 215, 0), 0.8f);

				for (int i = 0; i < 6; i++) {
					double theta = (2 * Math.PI * i) / 6;
					Vector offset = axisX.clone().multiply(Math.cos(theta) * radius)
							.add(axisY.clone().multiply(Math.sin(theta) * radius));

					for (double d = 0; d < distance; d += 1.5) {
						Location particleLoc = start.clone().add(dir.clone().multiply(d)).add(offset);
						world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, lineDust, true);
					}
				}

				// 4. 대미지 씹힘 문제 해결 (Step과 Radius 간격 조정)
				for (double d = 0; d <= distance; d += 1.5) { // < 에서 <= 로 변경하여 끝자락까지 확실히 체크
					Location checkLoc = start.clone().add(dir.clone().multiply(d));
					// 간격을 2.0 -> 1.5로 줄이고, 체크 반경을 1.2 -> 1.5로 늘려 사각지대(Gap) 제거
					for (Entity e : world.getNearbyEntities(checkLoc, 1.5, 1.5, 1.5)) {
						if (e instanceof LivingEntity le && !le.equals(caster) && !hitEntities.contains(le.getUniqueId())) {
							if (!pm.isParty(caster.getUniqueId(), le.getUniqueId())) {

								double currentDistance = start.distance(le.getLocation());
								double calculatedDamage = 4.0 + (currentDistance * 0.25);

								le.damage(calculatedDamage, caster);
								le.setFireTicks(80);
								hitEntities.add(le.getUniqueId());

								le.getWorld().playSound(le.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 1.5f);
							}
						}
					}
				}

				ticksLived++;
			}
		}.runTaskTimer(container.plugin(), 0L, 1L);
	}
}