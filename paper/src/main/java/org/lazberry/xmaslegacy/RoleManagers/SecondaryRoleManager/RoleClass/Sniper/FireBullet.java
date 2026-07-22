package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Sniper;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.StunUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.UUID;

@Skill(type = PlayerSkills.FIRE_BULLET)
public class FireBullet implements Skills<Sniper.Container>, UsingEnergy {
    private final @NotNull SkillEffectManager sem;
	private final @NotNull PartyManager pm;

	@Inject
    public FireBullet(@NotNull SkillEffectManager sem, @NotNull PartyManager pm) {
        this.sem = sem;
		this.pm = pm;
	}

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Sniper.@NotNull Container container) {
        UUID uuid = caster.getUniqueId();

        if (!container.reloaded.containsKey(uuid) && !container.magicalBullet.contains(uuid)) {
            InfoUtils.warn(caster, "장전되어있지 않습니다.");
            return false;
        }

        BulletType bullet = container.magicalBullet.contains(uuid) ? BulletType.MAGICAL : container.reloaded.get(uuid);
        Entity target = shoot(caster, container, bullet);

        if (BulletType.SNEAKY.equals(bullet) && target instanceof LivingEntity le) {
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));
        }
        return true;
    }

    @CanIgnoreReturnValue
    public @Nullable Entity shoot(@NotNull Player p, @NotNull Sniper.@NotNull Container container, @NotNull BulletType type) {
        UUID uuid = p.getUniqueId();

        if (!container.reloaded.containsKey(uuid) && !container.magicalBullet.contains(uuid)) {
            return null;
        }
        boolean magical = container.magicalBullet.contains(uuid);
        Entity target = null;

        if (magical) {
            container.magicalBullet.remove(uuid);
            type = BulletType.MAGICAL;
            target = fireSniperBullet(p, type.getDistance(), type.getDamage(), container);
            if (!(target instanceof LivingEntity le)) return target;

            activateSeal(le, container);
            p.playSound(p, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.2f);

            container.replaceSnipe(p);
            return le;
        }

        if (BulletType.STUN.equals(type)) {
            fireTravelingStunBullet(p, 1.3, type.getDistance(), type.getDamage(), container);
            p.playSound(p, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
        } else {
            target = fireSniperBullet(p, type.getDistance(), type.getDamage(), container);

            if (target != null) {
                container.lastHitRecord.put(target.getUniqueId(), type);

                p.sendActionBar(ColorUtils.chat("&6&lHIT! &f- " + target.getName()));
                p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
            }
        }

        container.reloaded.remove(uuid);
        container.replaceSnipe(p);

        return target;
    }

    public void activateSeal(@NotNull LivingEntity target, @NotNull Sniper.@NotNull Container container) {
        Location loc = target.getLocation().clone();
        World world = loc.getWorld();
        if (world == null) return;

        BlockDisplay display = world.spawn(loc, BlockDisplay.class, ent -> {
            ent.setBlock(Material.PURPLE_STAINED_GLASS.createBlockData());
            ent.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));

            ent.setTransformation(new org.bukkit.util.Transformation(
                    new org.joml.Vector3f(-1.5f, 0f, -1.5f),
                    new org.joml.AxisAngle4f(0, 0, 0, 1),
                    new org.joml.Vector3f(3f, 3f, 3f),
                    new org.joml.AxisAngle4f(0, 0, 0, 1)
            ));
        });
        Particle.DustOptions option = new Particle.DustOptions(Color.PURPLE, 1.0f);

        StunUtils.stun(target.getUniqueId(), 80L, "마법탄");
        sem.drawCircularLine(loc, Particle.DUST, 3, true, 70, option);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20, 1));
        world.playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);

        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 80;
            float yaw = loc.getYaw();

            @Override
            public void run() {
                if (ticks >= duration) {
                    display.setInterpolationDuration(20);
                    display.setInterpolationDelay(0);

                    display.setTransformation(new org.bukkit.util.Transformation(
                            new org.joml.Vector3f(0f, 1.5f, 0f),
                            new org.joml.AxisAngle4f(0, 0, 0, 1),
                            new org.joml.Vector3f(0.01f, 0.01f, 0.01f),
                            new org.joml.AxisAngle4f(0, 0, 0, 1)
                    ));

                    world.playSound(display.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
                    Bukkit.getScheduler().runTaskLater(container.plugin, display::remove, 20L);

                    this.cancel();
                    return;
                }

                yaw += 8;
                Location rotateLoc = display.getLocation();
                rotateLoc.setYaw(yaw);

                display.setTeleportDuration(1);
                display.teleport(rotateLoc);

                if (target.getLocation().distanceSquared(loc) > 1.0) {
                    target.teleport(loc);
                }

                ticks++;
            }
        }.runTaskTimer(container.plugin, 0L, 1L);
    }

    @CanIgnoreReturnValue
    public @Nullable Entity fireSniperBullet(Player p, double maxDistance, double damage, @NotNull Sniper.@NotNull Container container) {
        Location startLoc = p.getEyeLocation();
        org.bukkit.util.Vector direction = startLoc.getDirection();
        UUID uuid = p.getUniqueId();

        org.bukkit.util.RayTraceResult blockTrace = p.getWorld().rayTraceBlocks(startLoc, direction, maxDistance, FluidCollisionMode.NEVER, true);

        org.bukkit.util.RayTraceResult entityTrace = p.getWorld().rayTraceEntities(startLoc, direction, maxDistance, 0.2, (entity) ->
                entity instanceof LivingEntity && !entity.equals(p) && !pm.isParty(uuid, entity.getUniqueId())
        );

        double finalDistance = maxDistance;
        LivingEntity hitTarget = null;

        if (blockTrace != null && blockTrace.getHitBlock() != null) {
            finalDistance = startLoc.distance(blockTrace.getHitPosition().toLocation(p.getWorld()));
        }

        if (entityTrace != null && entityTrace.getHitEntity() instanceof LivingEntity target) {
            double entityDist = startLoc.distance(entityTrace.getHitPosition().toLocation(p.getWorld()));
            if (entityDist < finalDistance) {
                finalDistance = entityDist;
                hitTarget = target;
            }
        }

        org.bukkit.util.Vector step = direction.clone().normalize().multiply(0.3);
        Location particleLoc = startLoc.clone();

        BulletType currentType = container.reloaded.getOrDefault(uuid, BulletType.NORMAL);
        Particle.DustOptions trailColor = getTrailColor(currentType);

        for (double d = 0; d < finalDistance; d += 0.3) {
            particleLoc.add(step);
            p.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, trailColor);
        }

        if (hitTarget != null) {
            hitTarget.damage(damage, p);
            p.getWorld().spawnParticle(Particle.CRIT, hitTarget.getLocation().add(0, 1, 0), 15, 0.2, 0.2, 0.2, 0.5);
            p.getWorld().playSound(hitTarget.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);

            return hitTarget;
        } else {
            Location missLoc = startLoc.clone().add(direction.clone().multiply(finalDistance));
            p.getWorld().spawnParticle(Particle.BLOCK, missLoc, 5, 0.1, 0.1, 0.1, 0.1, Material.STONE.createBlockData());
        }

        p.getWorld().playSound(startLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
        return null;
    }

    private @NotNull Particle.DustOptions getTrailColor(@NotNull BulletType type) {
        return switch (type) {
            case MAGICAL -> new Particle.DustOptions(org.bukkit.Color.PURPLE, 2.0f);
            case SNEAKY -> new Particle.DustOptions(Color.GREEN, 1.2f);
            case STUN -> new Particle.DustOptions(Color.YELLOW, 1.2f);
            default -> new Particle.DustOptions(org.bukkit.Color.GRAY, 1.3f);
        };
    }

    public void fireTravelingStunBullet(Player p, double speedPerTick, double maxDistance, double damage, @NotNull Sniper.@NotNull Container container) {
        Location startLoc = p.getEyeLocation();
        org.bukkit.util.Vector direction = startLoc.getDirection().normalize();
        UUID uuid = p.getUniqueId();

        new BukkitRunnable() {
            private final Location currentLoc = startLoc.clone();
            private double distanceTraveled = 0;

            @Override
            public void run() {
                if (distanceTraveled >= maxDistance) {
                    cancel();
                    return;
                }

                RayTraceResult blockTrace = p.getWorld().rayTraceBlocks(currentLoc, direction, speedPerTick, FluidCollisionMode.NEVER, true);
                RayTraceResult entityTrace = p.getWorld().rayTraceEntities(currentLoc, direction, speedPerTick, 0.2, (entity) ->
                        entity instanceof LivingEntity && !entity.equals(p) && !pm.isParty(uuid, entity.getUniqueId())
                );

                double stepDistance = speedPerTick;
                LivingEntity hitTarget = null;
                boolean hitSomething = false;

                if (blockTrace != null && blockTrace.getHitBlock() != null) {
                    stepDistance = currentLoc.distance(blockTrace.getHitPosition().toLocation(p.getWorld()));
                    hitSomething = true;
                }

                if (entityTrace != null && entityTrace.getHitEntity() instanceof LivingEntity target) {
                    double entityDist = currentLoc.distance(entityTrace.getHitPosition().toLocation(p.getWorld()));
                    if (entityDist < stepDistance) {
                        stepDistance = entityDist;
                        hitTarget = target;
                        hitSomething = true;
                    }
                }

                org.bukkit.util.Vector stepVec = direction.clone().multiply(0.3);
                for (double d = 0; d < stepDistance; d += 0.3) {
                    currentLoc.add(stepVec);
                    p.getWorld().spawnParticle(Particle.DUST, currentLoc, 1, 0, 0, 0, 0, getTrailColor(BulletType.STUN));
                }

                if (hitSomething) {
                    if (hitTarget != null) {
                        StunUtils.stun(hitTarget.getUniqueId(), 40L, "스턴탄");
                        container.lastHitRecord.put(hitTarget.getUniqueId(), BulletType.STUN);

                        hitTarget.damage(damage, p);

                        p.sendActionBar(ColorUtils.chat("&e&lSTUN HIT! &f- " + hitTarget.getName()));
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f);
                        hitTarget.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, hitTarget.getLocation().add(0, 1, 0), 1);
                    } else {
                        p.getWorld().spawnParticle(Particle.BLOCK, currentLoc, 10, 0.1, 0.1, 0.1, 0.1, Material.STONE.createBlockData());
                        p.getWorld().playSound(currentLoc, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
                    }

                    cancel();
                    return;
                }

                distanceTraveled += speedPerTick;
            }
        }.runTaskTimer(container.plugin, 0L, 1L);
    }

    @Override
    public @NotNull SkillSet type() {
        return SecondarySkillSet.FIRE_BULLET;
    }

    @Override
    public @NotNull Role role() {
        return SecondaryRoles.SNIPER;
    }
}
