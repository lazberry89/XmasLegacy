package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Trapper;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.List;
import java.util.function.Consumer;

public final class BloodChainEffect {
    private final @NotNull XmasLegacy plugin;

    public BloodChainEffect(@NotNull XmasLegacy plugin) {
        this.plugin = plugin;
    }

    public void playEffect(@NotNull Player caster, @NotNull Consumer<LivingEntity> onHit, @NotNull Runnable onMiss) {
        final World world = caster.getWorld();
        final Location startLoc = caster.getEyeLocation().clone().add(0, -0.2, 0);
        final Vector direction = caster.getLocation().getDirection().normalize();

        Particle.DustOptions dustRed = new Particle.DustOptions(Color.fromRGB(150, 0, 0), 1.2f);
        Particle.DustOptions dustBrightRed = new Particle.DustOptions(Color.fromRGB(255, 50, 50), 1.2f);

        new BukkitRunnable() {
            double distance = 0;
            final double maxDistance = 15.0;
            final double speed = 0.8;
            double theta = 0;

            @Override
            public void run() {
                if (distance > maxDistance || !caster.isOnline()) {
                    onMiss.run();
                    this.cancel();
                    return;
                }

                Location currentMainLoc = startLoc.clone().add(direction.clone().multiply(distance));
                if (currentMainLoc.getBlock().isSolid()) {
                    onMiss.run();
                    this.cancel();
                    return;
                }

                List<LivingEntity> targets = world.getNearbyEntities(currentMainLoc, 0.6, 0.6, 0.6,
                                entity -> entity instanceof LivingEntity && entity != caster)
                        .stream()
                        .filter(entity -> !PartyManager.INSTANCE.isParty(entity.getUniqueId(), caster.getUniqueId()))
                        .map(entity -> (LivingEntity) entity)
                        .toList();

                if (!targets.isEmpty()) {
                    LivingEntity hitTarget = targets.getFirst();
                    onHit.accept(hitTarget);
                    this.cancel();
                    return;
                }

                Vector v1 = getPerpendicularVector(direction);
                Vector v2 = direction.getCrossProduct(v1);
                double radius = 0.3;

                Vector offset1 = v1.clone().multiply(Math.cos(theta) * radius).add(v2.clone().multiply(Math.sin(theta) * radius));
                Location particleLoc1 = currentMainLoc.clone().add(offset1);

                Vector offset2 = v1.clone().multiply(Math.cos(theta + Math.PI) * radius).add(v2.clone().multiply(Math.sin(theta + Math.PI) * radius));
                Location particleLoc2 = currentMainLoc.clone().add(offset2);

                world.spawnParticle(Particle.DUST, particleLoc1, 1, 0, 0, 0, 0, dustRed);
                world.spawnParticle(Particle.DUST, particleLoc2, 1, 0, 0, 0, 0, dustBrightRed);

                if ((int)(distance * 2) % 2 == 0) {
                    world.spawnParticle(Particle.DUST, particleLoc1.clone().add(particleLoc2).multiply(0.5), 1, 0, 0, 0, 0, dustBrightRed);
                }

                distance += speed;
                theta += Math.PI / 4;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * 주어진 벡터에 수직인 임의의 벡터를 반환합니다.
     */
    private @NotNull Vector getPerpendicularVector(@NotNull Vector dir) {
        if (Math.abs(dir.getX()) > Math.abs(dir.getZ())) {
            return new Vector(-dir.getY(), dir.getX(), 0).normalize();
        } else {
            return new Vector(0, -dir.getZ(), dir.getY()).normalize();
        }
    }
}
