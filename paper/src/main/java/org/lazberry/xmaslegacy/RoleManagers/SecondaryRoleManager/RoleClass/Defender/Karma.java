package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Defender;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.KARMA)
public class Karma implements Skills<Defender.Container>, UsingEnergy {
	private final @NotNull PartyManager pm;

	@Inject
	public Karma(@NotNull PartyManager pm) {
		this.pm = pm;
	}

	@Override
    public boolean execute(@NotNull Player caster, @NotNull Defender.@NotNull Container container) {
        if (!consumeEnergy(caster, 3)) return false;

        Location loc = caster.getLocation();
        SkillEffectManager.startHakiWave(container.plugin(), caster.getLocation());
        spawnShockWave(loc.clone().add(0, 0.5, 0), container);

        caster.getNearbyEntities(5, 5, 5).stream()
                .filter(e -> e != caster && e instanceof LivingEntity)
                .filter(e -> !pm.isParty(caster.getUniqueId(), e.getUniqueId()))
                .map(e -> (LivingEntity) e)
                .forEach(le -> {
                    le.damage(8.0, caster);

                    Vector pushVelocity = le.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5);
                    pushVelocity.setY(0.3);
                    le.setVelocity(pushVelocity);
                });
        caster.getWorld().playSound(caster, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.6f, 1.3f);
        return true;
    }

    public void spawnShockWave(@NotNull Location center, @NotNull Defender.@NotNull Container container) {
        new BukkitRunnable() {
            double radius = 0.5;
            final double maxRadius = 5.5;
            final double expansionSpeed = 1.1;

            @Override
            public void run() {
                if (radius > maxRadius) {
                    this.cancel();
                    return;
                }

                int particleCount = (int) (radius * 40);

                for (int i = 0; i < particleCount; i++) {
                    double angle = 2 * Math.PI * i / particleCount;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = center.clone().add(x, 0.1, z);

                    Particle.DustTransition option = new Particle.DustTransition(Color.RED, Color.BLACK, 1.3f);
                    center.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, option);

                    if (i % 3 == 0) {
                        center.getWorld().spawnParticle(Particle.CRIT, particleLoc, 1, 0, 0.1, 0, 0.05);
                    }
                }

                radius += expansionSpeed;
            }
        }.runTaskTimer(container.plugin(), 0L, 1L);
    }

    @Override
    public @NotNull SkillSet type() {
        return SecondarySkillSet.KARMA;
    }

    @Override
    public @NotNull Role role() {
        return SecondaryRoles.DEFENDER;
    }
}
