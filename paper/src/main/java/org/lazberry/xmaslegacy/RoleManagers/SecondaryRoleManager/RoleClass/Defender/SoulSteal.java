package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Defender;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.StunUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Skill(type = PlayerSkills.SOUL_STEAL)
@Registry.Exclude(type = ServerType.LOBBY)
public class SoulSteal implements Skills<Defender.Container>, UsingEnergy {
	private final @NotNull PartyManager pm;

	@Inject
	public SoulSteal(@NotNull PartyManager pm) {
		this.pm = pm;
	}

	@Override
    public boolean execute(@NotNull Player caster, @NotNull Defender.@NotNull Container container) {
        if (!consumeEnergy(caster, 3)) return false;
        Location loc = caster.getLocation();
        Location startLoc = loc.add(0, 1.2, 0);
        Vector dir = startLoc.getDirection().normalize();

        Vector axis;
        if (Math.abs(dir.getY()) > 0.9) {
            axis = new Vector(1, 0, 0);
        } else {
            axis = new Vector(-dir.getZ(), 0, dir.getX()).normalize();
        }

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.8f, 1.5f);

        new BukkitRunnable() {
            double distance = 0;
            final double maxDistance = 10.0;
            final double radius = 0.8;
            final Set<UUID> hitList = new HashSet<>();

            @Override
            public void run() {
                if (distance > maxDistance || !caster.isOnline()) {
                    this.cancel();
                    return;
                }

                for (double step = 0; step < 1.5; step += 0.15) {
                    distance += 0.15;
                    Location center = startLoc.clone().add(dir.clone().multiply(distance));

                    double angle = distance * 3.2;

                    Vector offset1 = axis.clone().rotateAroundAxis(dir, angle).multiply(radius);
                    Vector offset2 = axis.clone().rotateAroundAxis(dir, angle + Math.PI).multiply(radius);

                    caster.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(offset1), 1, 0, 0, 0, 0);
                    caster.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(offset2), 1, 0, 0, 0, 0);

                    rangeAttack(hitList, center, caster);
                }
            }
        }.runTaskTimer(container.plugin(), 0L, 1L);
        return true;
    }

    private void rangeAttack(@NotNull Set<UUID> hitList, @NotNull Location center, @NotNull Player caster) {
        center.getNearbyEntities(0.8, 0.8, 0.8).forEach(e -> {
            if (e instanceof LivingEntity target && !target.equals(caster)) {
                if (!pm.isParty(caster.getUniqueId(), target.getUniqueId())) {
                    if (hitList.add(target.getUniqueId())) {
                        target.damage(5.0, caster);
                        StunUtils.stun(target.getUniqueId(), 30L, "범위 공격");

                        target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.3f);
                    }

                }
            }
        });
    }

    @Override
    public @NotNull SkillSet type() {
        return SecondarySkillSet.SOUL_STEAL;
    }

    @Override
    public @NotNull Role role() {
        return SecondaryRoles.DEFENDER;
    }
}
