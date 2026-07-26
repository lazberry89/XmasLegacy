package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Warrior;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.TOMAHAWK)
@Registry.Exclude(type = ServerType.LOBBY)
@NoArgsConstructor
public class Tomahawk implements Skills<Warrior.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Warrior.@NotNull Container container) {
		if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;
		Location startLoc = caster.getEyeLocation();
		final Vector direction = startLoc.getDirection().clone().normalize().multiply(1.0);
		final float playerYaw = caster.getLocation().getYaw();

		ArmorStand axeStand = spawnAxe(caster, startLoc, playerYaw, container);
		startScheduler(caster, axeStand, direction, container);
		return true;
	}

	private void startScheduler(@NotNull Player caster, @NotNull ArmorStand axeStand, @NotNull Vector direction, @NotNull Warrior.@NotNull Container container) {
		new BukkitRunnable() {
			int ticks = 0;
			final int maxTicks = 40;

			@Override
			public void run() {
				if (ticks >= maxTicks || !axeStand.isValid()) {
					if (axeStand.isValid()) axeStand.remove();
					this.cancel();
					return;
				}

				Location currentLoc = axeStand.getLocation().add(direction);
				setEffects(axeStand, currentLoc, ticks);

				for (Entity entity : axeStand.getNearbyEntities(1.2, 1.2, 1.2)) {
					if (entity instanceof LivingEntity target && !entity.equals(caster)) {
						process(target, caster, axeStand, container);
						this.cancel();
						return;
					}
				}

				if (currentLoc.getBlock().getType().isSolid()) {
					axeStand.remove();
					this.cancel();
					return;
				}

				ticks++;
			}
		}.runTaskTimer(container.plugin(), 0L, 1L);
	}

	private void setEffects(@NotNull ArmorStand axeStand, @NotNull Location currentLoc, int ticks) {
		axeStand.teleport(currentLoc);
		axeStand.getWorld().spawnParticle(Particle.SWEEP_ATTACK, currentLoc, 3, 0.05, 0.05, 0.05, 0.01);

		double rotation = ticks * 0.6;
		axeStand.setRightArmPose(new EulerAngle(rotation, 0, 0));
	}

	private void process(@NotNull LivingEntity target, @NotNull Player caster, @NotNull ArmorStand axeStand, @NotNull Warrior.@NotNull Container container) {
		Location targetLoc = target.getLocation();
		Vector targetDir = targetLoc.getDirection().normalize();
		Location backLoc = targetLoc.clone().subtract(targetDir.multiply(1.5));

		if (backLoc.getBlock().getType().isSolid()) {
			backLoc.add(0, 1.0, 0);
		}

		caster.teleport(backLoc);
		caster.playSound(backLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
		target.damage(container.second_skill_damage(), caster);

		axeStand.remove();
	}

	private @NotNull ArmorStand spawnAxe(@NotNull Player caster, @NotNull Location startLoc, float playerYaw, @NotNull Warrior.@NotNull Container container) {
		return caster.getWorld().spawn(startLoc, ArmorStand.class, stand -> {
			stand.setVisible(false);
			stand.setGravity(false);
			stand.setArms(true);
			stand.setBasePlate(false);
			stand.setMarker(true);
			GlowUtils.glow(stand, NamedTextColor.RED);

			Location loc = stand.getLocation();
			loc.setYaw(playerYaw);
			stand.teleport(loc);

			stand.getEquipment().setItemInMainHand(new ItemStack(container.weapon_item()));
		});
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.TOMAHAWK;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.WARRIOR;
	}
}
