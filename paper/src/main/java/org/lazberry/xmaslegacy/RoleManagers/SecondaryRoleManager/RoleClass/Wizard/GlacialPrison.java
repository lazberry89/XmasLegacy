package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Wizard;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.Utils.StunUtils;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.WIZARD;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.GLACIAL_PRISON;

@Skill(type = PlayerSkills.GLACIAL_PRISON)
public class GlacialPrison implements Skills<Wizard.Container>, UsingEnergy {
    private final @NotNull BlockData iceBlock;
    private static final float PROJECTILE_SCALE = 1.5f;
    private static final int PRISON_DURATION_TICKS = 60;

    @ApiStatus.Internal
    private GlacialPrison() {
        this.iceBlock = Material.ICE.createBlockData();
    }

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Wizard.Container container) {
        if (!consumeEnergy(caster, 5)) return false;

        final double speed = Math.max(0.4, 0);
        final Location startLoc = caster.getEyeLocation();
        final Vector velocity = caster.getLocation().getDirection().normalize().multiply(speed);

        startLoc.getWorld().playSound(startLoc, Sound.ENTITY_BREEZE_CHARGE, 1.0f, 0.8f);
        launchProjectile(caster, startLoc, velocity, speed, container);
        return true;
    }

    private @NotNull BlockDisplay spawnIceDisplay(@NotNull Location loc, float scale) {
        Vector3f scaleVec = new Vector3f(scale, scale, scale);
        Vector3f pivotTranslation = new Vector3f(-scale / 2f, -scale / 2f, -scale / 2f);

        Transformation transform = new Transformation(pivotTranslation, new AxisAngle4f(), scaleVec, new AxisAngle4f());

        return loc.getWorld().spawn(loc, BlockDisplay.class, display -> {
            display.setBlock(iceBlock);
            display.setInterpolationDuration(1);
            display.setTransformation(transform);
            GlowUtils.glow(display, NamedTextColor.BLUE);
        });
    }

    private void launchProjectile(@NotNull Player caster, @NotNull Location startLoc, @NotNull Vector velocity, double speed, @NotNull Wizard.Container container) {
        BlockDisplay display1 = spawnIceDisplay(startLoc, PROJECTILE_SCALE);
        BlockDisplay display2 = spawnIceDisplay(startLoc, PROJECTILE_SCALE);

        new BukkitRunnable() {
            private final Location currentLoc = startLoc.clone();
            private int ticks = 0;
            private float angle = 0.0f;

            @Override
            public void run() {
                if (ticks > 50 || !caster.isOnline()) {
                    cleanup();
                    return;
                }

                Vector targetDir = caster.getLocation().getDirection().normalize().multiply(speed);
                velocity.multiply(0.90).add(targetDir.multiply(0.10)).normalize().multiply(speed);

                var rayTrace = currentLoc.getWorld().rayTrace(
                        currentLoc, velocity, speed, FluidCollisionMode.ALWAYS, true, 0.7,
                        entity -> (entity instanceof LivingEntity && entity != caster)
                );

                if (rayTrace != null) {
                    handleHit(rayTrace, caster, container);
                    cleanup();
                    return;
                }

                currentLoc.add(velocity);
                display1.teleport(currentLoc);
                display2.teleport(currentLoc);

                angle += 0.15f;
                updateDisplayRotation(display1, display2, PROJECTILE_SCALE, angle);

                currentLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLoc, 2, 0.2, 0.2, 0.2, 0.01);
                ticks++;
            }

            private void cleanup() {
                if (display1.isValid()) display1.remove();
                if (display2.isValid()) display2.remove();
                this.cancel();
            }
        }.runTaskTimer(container.plugin(), 0L, 1L);
    }

    private void handleHit(@NotNull RayTraceResult rayTrace, @NotNull Player caster, @NotNull Wizard.Container container) {
        var world = caster.getWorld();
        Location hitLoc = rayTrace.getHitPosition().toLocation(world);

        if (rayTrace.getHitEntity() instanceof LivingEntity victim) {
            createGlacialPrison(victim, caster, container);
        } else if (rayTrace.getHitBlock() != null) {
            world.spawnParticle(Particle.BLOCK, hitLoc, 20, 0.3, 0.3, 0.3, 0.1, iceBlock);
            world.playSound(hitLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
        }
    }

    /**
     * 대상에게 스턴을 부여하고 얼음 구체 내부에 완전히 가두는 핵심 비즈니스 로직
     */
    private void createGlacialPrison(@NotNull LivingEntity victim, @NotNull Player caster, @NotNull Wizard.Container container) {
        var world = victim.getWorld();
        Location lockLoc = victim.getLocation().clone();

        StunUtils.stun(victim.getUniqueId(), PRISON_DURATION_TICKS, "마법사의 얼음감옥");

        if (victim instanceof Player targetPlayer) {
            var user = UserManager.INSTANCE.getUser(targetPlayer.getUniqueId());
            if (user != null && !user.isImmuneToIcing()) {
                user.addIcingState(-10);
            }
        }

        float cageScale = 2.2f;
        BlockDisplay cage1 = spawnIceDisplay(lockLoc, cageScale);
        BlockDisplay cage2 = spawnIceDisplay(lockLoc, cageScale);

        new BukkitRunnable() {
            private int elapsed = 0;
            private float rotation = 0.0f;

            @Override
            public void run() {
                if (elapsed > PRISON_DURATION_TICKS || victim.isDead()) {
                    executeShatterFinale(lockLoc, victim, caster);
                    cage1.remove();
                    cage2.remove();
                    this.cancel();
                    return;
                }

                victim.teleport(lockLoc);
                victim.setFreezeTicks(400);

                rotation += 0.04f;
                updateDisplayRotation(cage1, cage2, cageScale, rotation);

                world.spawnParticle(Particle.WHITE_SMOKE, lockLoc.clone().add(0, 1, 0), 2, 0.2, 0.4, 0.2, 0.01);
                elapsed++;
            }
        }.runTaskTimer(container.plugin(), 0L, 1L);
    }

    private void executeShatterFinale(@NotNull Location loc, @NotNull LivingEntity victim, @NotNull Player caster) {
        var world = loc.getWorld();

        // 사운드 및 파티클 디자인
        world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 2.0f, 0.5f); // 묵직하게 깨지는 로우 피치
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.4f);
        world.spawnParticle(Particle.BLOCK, loc.clone().add(0, 1, 0), 70, 0.6, 0.6, 0.6, 0.1, iceBlock);
        world.spawnParticle(Particle.EXPLOSION, loc, 1, 0, 0, 0, 0);

        victim.damage(14.0, caster);
    }

    private void updateDisplayRotation(@NotNull BlockDisplay d1, @NotNull BlockDisplay d2, float scale, float currentAngle) {
        Vector3f pivot = new Vector3f(-scale / 2f, -scale / 2f, -scale / 2f);
        Vector3f scaleVec = new Vector3f(scale, scale, scale);

        d1.setTransformation(new Transformation(pivot, new AxisAngle4f(currentAngle, 1, 1, 0), scaleVec, new AxisAngle4f()));
        d2.setTransformation(new Transformation(pivot, new AxisAngle4f(-currentAngle, 0, 1, 1), scaleVec, new AxisAngle4f()));
    }

    @Override
    public @NotNull SkillSet type() {
        return GLACIAL_PRISON;
    }

    @Override
    public @NotNull Role role() {
        return WIZARD;
    }
}