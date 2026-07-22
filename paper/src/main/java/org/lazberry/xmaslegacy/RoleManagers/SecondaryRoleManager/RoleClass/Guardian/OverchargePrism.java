package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Guardian;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.GUARDIAN;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.OVERCHARGE_PRISM;

@Skill(type = PlayerSkills.OVERCHARGE_PRISM)
public class OverchargePrism implements Skills<Guardian.Container>, UsingEnergy {
	private final @NotNull PartyManager pm;

	@Inject
	public OverchargePrism(@NotNull PartyManager pm) {
		this.pm = pm;
	}

	@Override
    public boolean execute(@NotNull Player caster, @NotNull Guardian.@NotNull Container container) {
        LivingEntity target = container.targetMap.get(caster);
        if (target == null) {
            InfoUtils.error(caster, "연결된 타겟이 없습니다!");
            return false;
        }
        if (!consumeEnergy(caster, 4)) return false;

        Location center = target.getLocation();

        Vector[] directions = {
                new Vector( 1, 0,  1).normalize(),
                new Vector( 1, 0, -1).normalize(),
                new Vector(-1, 0,  1).normalize(),
                new Vector(-1, 0, -1).normalize()
        };

        Set<UUID> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            double distance = 0;
            final double maxDistance = 12.0;

            @Override
            public void run() {
                if (distance >= maxDistance) {
                    this.cancel();
                    return;
                }

                for (Vector dir : directions) {
                    Location point = center.clone().add(dir.clone().multiply(distance));

                    point.getWorld().spawnParticle(Particle.SWEEP_ATTACK, point, 1, 0, 0, 0, 0);
                    point.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, point, 2, 0.1, 0.1, 0.1, 0);

                    point.getWorld().playSound(point, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.5f); // 볼륨 낮게
                    point.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, point, 1, 0, 0, 0, 0);

                    for (Entity e : point.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                        if (e instanceof LivingEntity le
                                && !e.equals(caster)
                                && !pm.isParty(caster.getUniqueId(), e.getUniqueId())
                                && !hitEntities.contains(e.getUniqueId())) {
                            le.damage(8.0, caster);
                            hitEntities.add(e.getUniqueId());
                        }
                    }
                }
                distance += 1.5;
            }
        }.runTaskTimer(container.plugin, 0L, 1L);

        return true;
    }

    @Override
    public @NotNull SkillSet type() {
        return OVERCHARGE_PRISM;
    }

    @Override
    public @NotNull Role role() {
        return GUARDIAN;
    }
}
