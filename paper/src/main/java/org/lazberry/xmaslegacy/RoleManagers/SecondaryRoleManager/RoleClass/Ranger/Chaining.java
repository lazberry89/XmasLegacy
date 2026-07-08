package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Ranger;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.*;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.RANGER;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.CHAINING;

@Skill(type = PlayerSkills.CHAINING)
public class Chaining implements Skills<Ranger.Container>, UsingEnergy {
	private final @NotNull Map<UUID, ActiveLaserTask> activeLasers = new HashMap<>();

	@Override
	public boolean execute(@NotNull Player caster, Ranger.@NotNull Container container) {
		ItemStack tool = caster.getInventory().getItemInMainHand();

		if (activeLasers.containsKey(caster.getUniqueId())) {
			ActiveLaserTask laser = activeLasers.remove(caster.getUniqueId());
			laser.triggerCombo();
			return false;
		}

		if (!consumeEnergy(caster, 3)) return false;

		caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 2.0f);
		InfoUtils.info(caster, "5초 이내에 재입력 : 2차이동 스킬 시전");

		ActiveLaserTask task = new ActiveLaserTask(caster, tool, container);
		activeLasers.put(caster.getUniqueId(), task);
		task.runTaskTimer(container.plugin(), 0L, 1L);

		return false;
	}

	private class ActiveLaserTask extends BukkitRunnable {
		private final @NotNull Player p;
		private final @NotNull ItemStack tool;
		private final @NotNull Ranger.Container container;

		private int ticks = 0;
		private Location currentLoc;
		private Vector direction;
		private final @NotNull List<Location> trailPoints = new ArrayList<>();
		private Location lastSaved;

		private final double SPEED = 1.5;
		private final int MAX_TICKS = 100;

		public ActiveLaserTask(@NotNull Player p, @NotNull ItemStack tool, @NotNull Ranger.Container container) {
			this.p = p;
			this.tool = tool;
			this.container = container;
			this.currentLoc = p.getEyeLocation();
			this.direction = currentLoc.getDirection().normalize();
			this.lastSaved = currentLoc.clone();
		}

		@Override
		public void run() {
			if (!p.isOnline() || p.isDead()) {
				cleanup();
				return;
			}

			if (ticks >= MAX_TICKS) {
				p.sendActionBar(ColorUtils.chat("&c레이저 지속시간 만료"));
				cleanup();
				return;
			}

			ticks++;

			RayTraceResult hit = p.getWorld().rayTraceBlocks(currentLoc, direction, SPEED, FluidCollisionMode.ALWAYS, true);
			Location nextLoc;

			if (hit != null && hit.getHitBlock() != null) {
				nextLoc = hit.getHitPosition().toLocation(p.getWorld());
				drawLaserTrail(currentLoc, nextLoc);
				direction = calculateReflection(direction, hit.getHitBlockFace());

				currentLoc = nextLoc.clone().add(direction.clone().multiply(0.15));
				p.getWorld().playSound(currentLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 2.0f);
			} else {
				nextLoc = currentLoc.clone().add(direction.clone().multiply(SPEED));
				drawLaserTrail(currentLoc, nextLoc);
				currentLoc = nextLoc;
			}
		}

		private void drawLaserTrail(@NotNull Location start, @NotNull Location end) {
			double distance = start.distance(end);
			Vector step = end.toVector().subtract(start.toVector()).normalize().multiply(0.2);

			Location tempLoc = start.clone();
			Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW, 0.8f);

			for (double d = 0; d < distance; d += 0.2) {
				tempLoc.add(step);
				tempLoc.getWorld().spawnParticle(Particle.DUST, tempLoc, 1, 0, 0, 0, 0, dust, true);

				if (tempLoc.distance(lastSaved) >= 1.5) {
					trailPoints.add(tempLoc.clone());
					lastSaved = tempLoc.clone();
				}
			}
		}

		private @NotNull Vector calculateReflection(@NotNull Vector direction, @Nullable BlockFace hitFace) {
			if (hitFace == null) return direction.multiply(-1);
			Vector normal = hitFace.getDirection().normalize();
			double dotProduct = direction.dot(normal);
			return direction.subtract(normal.multiply(2 * dotProduct)).normalize();
		}

		private void cleanup() {
			activeLasers.remove(p.getUniqueId());
			if (p.isOnline()) {
				p.setCooldown(tool, 60);
			}
			this.cancel();
		}

		public void triggerCombo() {
			this.cancel();

			Location dest = currentLoc.clone();
			dest.setYaw(p.getLocation().getYaw());
			dest.setPitch(p.getLocation().getPitch());

			p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
			p.teleport(dest);
			p.getWorld().playSound(dest, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.2f);
			p.sendActionBar(ColorUtils.chat("&#FF4545L&#FD5040A&#FB5B3AS&#F96735E&#F7722FR &#F48824T&#F2931FR&#F09E19A&#EEAA14I&#ECB50EL&#EAC009!"));

			for (Location trailLoc : trailPoints) {
				trailLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, trailLoc, 1, 0, 0, 0, 0, null, true);
				trailLoc.getWorld().playSound(trailLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.3f); // 겹치면 시끄러우니 볼륨 0.4

				trailLoc.getWorld().getNearbyEntities(trailLoc, 1.5, 1.5, 1.5).forEach(e -> {
					if (e instanceof LivingEntity le && !le.equals(p)) {
						le.damage(8.0, p);
						SkillEffectManager.INSTANCE.StunEntity(le.getUniqueId(), 20L);
					}
				});
			}
			p.setCooldown(tool, 60);
		}
	}

	@Override
	public @NotNull SkillSet type() {
		return CHAINING;
	}

	@Override
	public @NotNull Role role() {
		return RANGER;
	}
}