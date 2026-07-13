package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Wizard;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.WIZARD;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.MANA_ORB;

@Skill(type = PlayerSkills.MANA_ORB)
public class ManaOrb implements Skills<Wizard.Container>, UsingEnergy {
    private final @NotNull BlockData glassBlock;

    @ApiStatus.Internal
    private ManaOrb() {
        this.glassBlock = Material.LIGHT_BLUE_STAINED_GLASS.createBlockData();
    }

    private @NotNull BlockDisplay spawnEffect(@NotNull Location startLoc, @NotNull Vector3f pivotTranslation, @NotNull Vector3f scaleVec) {
        Transformation initTransform = new Transformation(
                pivotTranslation,
                new AxisAngle4f(),
                scaleVec,
                new AxisAngle4f()
        );
        return startLoc.getWorld().spawn(startLoc, BlockDisplay.class, d1 -> {
            d1.setBlock(glassBlock);
            d1.setInterpolationDuration(1);
            d1.setTransformation(initTransform);
            GlowUtils.glow(d1, NamedTextColor.AQUA);
        });
    }

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Wizard.Container container) {
        if (!consumeEnergy(caster, 3)) return false;

        final double speed = Math.max(0.5, 0);
        final Location startLoc = caster.getEyeLocation();
        final Vector bulletVelocity = caster.getLocation().getDirection().normalize().multiply(speed);

        var world = startLoc.getWorld();
        caster.playSound(startLoc, Sound.ENTITY_BOAT_PADDLE_WATER, 1.0f, 1.5f);

        float scale = 0.6f;
        Vector3f scaleVec = new Vector3f(scale, scale, scale);
        Vector3f pivotTranslation = new Vector3f(-scale / 2f, -scale / 2f, -scale / 2f);

        BlockDisplay display1 = spawnEffect(startLoc, pivotTranslation, scaleVec);
        BlockDisplay display2 = spawnEffect(startLoc, pivotTranslation, scaleVec);

        startScheduler(caster, startLoc, world, bulletVelocity, display1, display2, scaleVec, pivotTranslation, speed, container);

        return true;
    }

    private void startScheduler(@NotNull Player caster,
                                @NotNull Location startLoc,
                                @NotNull World world,
                                @NotNull Vector bulletVelocity,
                                @NotNull BlockDisplay display1,
                                @NotNull BlockDisplay display2,
                                @NotNull Vector3f scaleVec,
                                @NotNull Vector3f pivotTranslation,
                                double speed,
                                @NotNull Wizard.Container container) {
        new BukkitRunnable() {
            private final Location bulletLoc = startLoc.clone();
            private int ticks = 0;
            private float rotationAngle = 0.0f;

            @Override
            public void run() {
                if (ticks > 40 || !caster.isOnline()) {
                    cleanup();
                    return;
                }

                Vector targetDir = caster.getLocation().getDirection().normalize().multiply(speed);
                bulletVelocity.multiply(0.85).add(targetDir.multiply(0.15));
                if (bulletVelocity.lengthSquared() > 0) {
                    bulletVelocity.normalize().multiply(speed);
                }

                var rayTrace = world.rayTrace(
                        bulletLoc,
                        bulletVelocity,
                        speed,
                        FluidCollisionMode.ALWAYS,
                        true,
                        0.4,
                        entity -> (entity instanceof LivingEntity ignored && entity != caster)
                );

                if (rayTrace != null) {
                    hitEffect(rayTrace, world, caster);
                    cleanup();
                    return;
                }

                bulletLoc.add(bulletVelocity);
                display1.teleport(bulletLoc);
                display2.teleport(bulletLoc);

                rotationAngle += 0.3f;

                applyTransformation(display1, display2, pivotTranslation, scaleVec, rotationAngle);

                world.spawnParticle(Particle.DRIPPING_WATER, bulletLoc, 3, 0.1, 0.1, 0.1, 0.0);
                world.spawnParticle(Particle.SNOWFLAKE, bulletLoc, 1, 0.05, 0.05, 0.05, 0.01);
                if (ticks % 2 == 0) world.spawnParticle(Particle.FALLING_WATER, bulletLoc, 2, 0.1, 0.1, 0.1, 0.0);

                ticks++;
            }

            private void cleanup() {
                if (display1.isValid()) display1.remove();
                if (display2.isValid()) display2.remove();
                this.cancel();
            }
        }.runTaskTimer(container.plugin(), 0L, 1L);
    }

    private void hitEffect(@NotNull RayTraceResult rayTrace, @NotNull World world, @NotNull Player caster) {
        var hitEntity = rayTrace.getHitEntity();
        var hitBlock = rayTrace.getHitBlock();
        Location hitLoc = rayTrace.getHitPosition().toLocation(world);

        if (hitEntity instanceof LivingEntity victim) {
            victim.damage(5.0, caster);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));

            world.spawnParticle(Particle.DRIPPING_WATER, hitLoc, 30, 0.3, 0.3, 0.3, 0.2);
            world.spawnParticle(Particle.SPLASH, hitLoc, 20, 0.2, 0.2, 0.2, 0.1);
            caster.playSound(caster, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.0f, 1.0f);
        } else if (hitBlock != null) {
            world.spawnParticle(Particle.DRIPPING_WATER, hitLoc, 15, 0.2, 0.2, 0.2, 0.1);
            caster.playSound(caster, Sound.BLOCK_WATER_AMBIENT, 0.8f, 1.3f);
        }
    }

    private void applyTransformation(@NotNull BlockDisplay display1,
                                     @NotNull BlockDisplay display2,
                                     @NotNull Vector3f pivotTranslation,
                                     @NotNull Vector3f scaleVec,
                                     float rotationAngle) {
        Transformation t1 = new Transformation(
                pivotTranslation,
                new AxisAngle4f(rotationAngle, 1, 0, 1),
                scaleVec,
                new AxisAngle4f()
        );
        Transformation t2 = new Transformation(
                pivotTranslation,
                new AxisAngle4f(-rotationAngle, 0, 1, 1),
                scaleVec,
                new AxisAngle4f()
        );

        display1.setTransformation(t1);
        display2.setTransformation(t2);
    }

    @Override
    public @NotNull SkillSet type() {
        return MANA_ORB;
    }

    @Override
    public @NotNull Role role() {
        return WIZARD;
    }
}
