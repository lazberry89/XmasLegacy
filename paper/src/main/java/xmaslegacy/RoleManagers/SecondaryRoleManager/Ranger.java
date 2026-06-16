package xmaslegacy.RoleManagers.SecondaryRoleManager;

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
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import xmaslegacy.Annotation.Roles;
import xmaslegacy.Emblems.EmblemType;
import xmaslegacy.RoleManagers.UsingEnergy;
import xmaslegacy.SkillEffectManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Roles(grade = 2)
public class Ranger extends AbstractSecondRole {
    private final @NotNull SkillEffectManager sem;
    private final @NotNull Map<UUID, List<Location>> recentTrails = new HashMap<>();
    private final @NotNull Map<UUID, Long> trailTimestamps = new HashMap<>();

    public Ranger() {
        super(SecondaryRoles.RANGER);
		this.sem = SkillEffectManager.INSTANCE;
    }

    @Override
    public void useFirstSkill(@NotNull Player p) {
        if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!consumeEnergy(p, 3)) return;

        p.setCooldown(tool, 60);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 2.0f);

        launchLaserTrajectory(p);
    }

    private void launchLaserTrajectory(@NotNull Player p) {
        List<Location> trailPoints = new java.util.ArrayList<>();

        new BukkitRunnable() {
            int ticks = 0;
            int bounces = 0;
            Location currentLoc = p.getEyeLocation();
            Vector direction = currentLoc.getDirection().normalize();

            final double SPEED = 1.5;
            final int MAX_TICKS = 80;
            final int MAX_BOUNCES = 5;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) {
                    this.cancel();
                    return;
                }

                if (ticks >= MAX_TICKS || bounces >= MAX_BOUNCES) {
                    finalizeTeleport(p, currentLoc, trailPoints);
                    this.cancel();
                    return;
                }

                ticks++;

                RayTraceResult hit = p.getWorld().rayTraceBlocks(currentLoc, direction, SPEED, FluidCollisionMode.ALWAYS, true);

                Location nextLoc;
                if (hit != null && hit.getHitBlock() != null) {
                    nextLoc = hit.getHitPosition().toLocation(p.getWorld());
                    drawLaserTrail(currentLoc, nextLoc, trailPoints);
                    direction = calculateReflection(direction, hit.getHitBlockFace());

                    currentLoc = nextLoc.clone().add(direction.clone().multiply(0.15));
                    bounces++;

                    p.getWorld().playSound(currentLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 2.0f);

                    if (bounces >= MAX_BOUNCES) {
                        finalizeTeleport(p, currentLoc, trailPoints);
                        this.cancel();
                    }
                } else {
                    nextLoc = currentLoc.clone().add(direction.clone().multiply(SPEED));
                    drawLaserTrail(currentLoc, nextLoc, trailPoints);
                    currentLoc = nextLoc;
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 1L);
    }

    private @NotNull Vector calculateReflection(@NotNull Vector direction, @Nullable BlockFace hitFace) {
        if (hitFace == null) return direction.multiply(-1);

        Vector normal = hitFace.getDirection().normalize();
        double dotProduct = direction.dot(normal);

        return direction.subtract(normal.multiply(2 * dotProduct)).normalize();
    }

    private void drawLaserTrail(@NotNull Location start, @NotNull Location end, @NotNull List<Location> trailPoints) {
        double distance = start.distance(end);
        Vector step = end.toVector().subtract(start.toVector()).normalize().multiply(0.2);
        Location tempLoc = start.clone();

        Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW, 0.8f);

        for (double d = 0; d < distance; d += 0.2) {
            tempLoc.add(step);
            tempLoc.getWorld().spawnParticle(Particle.DUST, tempLoc, 1, 0, 0, 0, 0, dust);
            if (Math.round(d * 10) % 10 == 0) trailPoints.add(tempLoc.clone());
        }
    }

    /**
     * 스킬의 최종 결과: 플레이어를 도착 지점으로 순간이동 시킵니다.
     */
    private void finalizeTeleport(@NotNull Player p, @NotNull Location targetLoc, @NotNull List<Location> trailPoints) {
        Location dest = targetLoc.clone();

        dest.setYaw(p.getLocation().getYaw());
        dest.setPitch(p.getLocation().getPitch());

        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation(), 30, 0.5, 1, 0.5, 0.1);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);

        p.teleport(dest);
        playDoubleVerticalChainExplosion(dest, p);

        p.getWorld().spawnParticle(Particle.PORTAL, dest, 30, 0.5, 1, 0.5, 0.1);
        p.getWorld().spawnParticle(Particle.FLASH, dest, 1);
        p.getWorld().playSound(dest, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.2f);

        recentTrails.put(p.getUniqueId(), trailPoints);
        trailTimestamps.put(p.getUniqueId(), System.currentTimeMillis());
    }

    private void playDoubleVerticalChainExplosion(@NotNull Location baseLoc, @NotNull Player caster) {
        new BukkitRunnable() {
            int offset = 0;
            final int maxDistance = 12;

            @Override
            public void run() {
                if (offset > maxDistance) {
                    this.cancel();
                    return;
                }

                Location upLoc = baseLoc.clone().add(0, offset, 0);
                spawnExplosionEffect(upLoc, 0.5f + (offset * 0.1f), caster);

                if (offset > 0) {
                    Location downLoc = baseLoc.clone().subtract(0, offset, 0);
                    spawnExplosionEffect(downLoc, 0.5f + (offset * 0.1f), caster);
                }

                offset++;
            }
        }.runTaskTimer(getPlugin(), 0L, 2L);
    }

    private void spawnExplosionEffect(@NotNull Location loc, float pitch, @NotNull Player caster) {
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.3f, pitch);

        loc.getWorld().getNearbyEntities(loc, 1.0, 1.0, 1.0).forEach(e -> {
            if (e instanceof LivingEntity le && !le.equals(caster)) sem.StunEntity(le.getUniqueId());
        });
    }

    @Override
    public void useSecondSkill(@NotNull Player p) {
        if (isSkillCancelled(p, this, emblem, EmblemType.RANGE)) return;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!consumeEnergy(p, 4)) return;

        p.setCooldown(tool, 60);
        UUID uuid = p.getUniqueId();
        boolean hasComboTrail = false;

        if (recentTrails.containsKey(uuid) && trailTimestamps.containsKey(uuid)) {
            long timePassed = System.currentTimeMillis() - trailTimestamps.get(uuid);
            if (timePassed <= 5000) {
                hasComboTrail = true;
            }
        }

        if (hasComboTrail) {
            List<Location> points = recentTrails.get(uuid);

            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.5f);
            p.sendActionBar(ColorUtils.chat("&e&l[ 잔상 폭발! ]"));

            new BukkitRunnable() {
                int index = points.size() - 1;

                @Override
                public void run() {
                    if (index < 0 || !p.isOnline()) {
                        this.cancel();
                        return;
                    }

                    for (int i = 0; i < 2; i++) {
                        if (index < 0) break;
                        Location loc = points.get(index);

                        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0);
                        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.2f);

                        loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5).forEach(e -> {
                            if (e instanceof LivingEntity le && !le.equals(p)) le.damage(5.0, p);
                        });
                        index--;
                    }
                }
            }.runTaskTimer(getPlugin(), 0L, 1L);

            recentTrails.remove(uuid);
            trailTimestamps.remove(uuid);

        } else {
            p.setNoDamageTicks(10);
            Location frontLoc = p.getLocation().add(p.getLocation().getDirection().normalize().multiply(1.0));
            frontLoc.add(0, 1, 0);

            p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, frontLoc, 1, 0, 0, 0, 0);
            p.getWorld().spawnParticle(Particle.FLASH, frontLoc, 1);
            p.getWorld().playSound(frontLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);

            Vector pushBack = p.getLocation().getDirection().normalize().multiply(-2.1);
            pushBack.setY(0.6);

            p.setVelocity(pushBack);

            frontLoc.getWorld().getNearbyEntities(frontLoc, 2.0, 2.0, 2.0).forEach(e -> {
                if (e instanceof LivingEntity le && !le.equals(p)) {
                    le.damage(4.0, p);
                    le.setVelocity(p.getLocation().getDirection().normalize().multiply(1.2));
                }
            });
        }
    }

    @Override
    public void usePassive(@NotNull Player p) {

    }

    @Override
    public @NotNull ItemStack roleWeapon() {
        return new ItemStack(Material.STICK);
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return new ItemStack(Material.LEATHER_BOOTS);
    }

    @Override
    public @NotNull ItemStack TargetEmblem() {
        return emblem.getTargetEmblem();
    }

    @Override
    public @NotNull ItemStack RangeEmblem() {
        return emblem.getRangeEmblem();
    }
}
