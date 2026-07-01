package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Skills.Archer;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

public class ShockDart implements Skills<Archer.Container> {

	@Override
	public void execute(@NotNull Player p, @NotNull Archer.Container container) {
		p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.6f);

		Arrow arrow = p.launchProjectile(Arrow.class);
		arrow.setVelocity(p.getLocation().getDirection().multiply(container.first_skill_arrow_speed()));
		arrow.setShooter(p);
		arrow.getPersistentDataContainer().set(KeyUtils.get("skill"), PersistentDataType.STRING, "archer_arrow");
		arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (!arrow.isValid() || arrow.isOnGround()) {
					this.cancel();
					return;
				}
				arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 3, 0.05, 0.05, 0.05, 0.01);
			}
		}.runTaskTimer(container.plugin(), 0L, 1L);
		Bukkit.getScheduler().runTaskLater(container.plugin(), () -> {
			if (arrow.isValid()) {
				arrow.remove();
			}
		}, container.first_skill_arrow_timeout());
	}


	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.SHOCK_DART;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.ARCHER;
	}
}
