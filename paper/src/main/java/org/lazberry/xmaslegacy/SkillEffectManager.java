package org.lazberry.xmaslegacy;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public enum SkillEffectManager {
	INSTANCE;

    private final @NotNull XmasLegacy plugin;
    private final @NotNull Set<UUID> immuneToKnockback = new HashSet<>();
    private final @NotNull Set<UUID> immuneToDebuff = new HashSet<>();
    private final @NotNull Set<LivingEntity> hideMap = new HashSet<>();

    SkillEffectManager() {
        this.plugin = XmasLegacy.getInstance();
    }

    public void setImmuneToKnockback(@NotNull UUID uuid, boolean flag) {
        if (flag) this.immuneToKnockback.add(uuid);
        else this.immuneToKnockback.remove(uuid);
    }

    public boolean isImmuneToKnockback(@NotNull UUID uuid) {
        return this.immuneToKnockback.contains(uuid);
    }

    public void setImmuneToDebuff(@NotNull UUID uuid, boolean flag) {
        if (flag) this.immuneToDebuff.add(uuid);
        else this.immuneToDebuff.remove(uuid);
    }

    public boolean isImmuneToDebuff(@NotNull UUID uuid) {
        return this.immuneToDebuff.contains(uuid);
    }

    private static final Set<PotionEffectType> DEBUFFS = Set.of(
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.BLINDNESS,
            PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS,
            PotionEffectType.NAUSEA,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION,
            PotionEffectType.BAD_OMEN,
            PotionEffectType.DARKNESS
    );

    public static void clearDebuffs(@NotNull Player p) {
        for (PotionEffectType debuffType : DEBUFFS) {
            if (p.hasPotionEffect(debuffType))
                p.removePotionEffect(debuffType);
        }
    }

    public void hideEntity(LivingEntity le) {
        this.hideMap.add(le);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hideEntity(plugin, le);
        }
        if (le instanceof Player player) {
            player.setInvisible(true);

            for (Entity nearby : player.getNearbyEntities(32, 32, 32)) {
                if (nearby instanceof Mob mob && player.equals(mob.getTarget())) {
                    mob.setTarget(null);
                }
            }
        }
    }
    public @NotNull Set<LivingEntity> getHiddenEntity() {
        return this.hideMap;
    }
    public void showEntity(@NotNull LivingEntity le) {
        if (this.hideMap.remove(le)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showEntity(plugin, le);
            }
            if (le instanceof Player player) {
                player.setInvisible(false);
            }
        }
    }

    public void knockbackEntity(Player player, LivingEntity target, double force, double yForce) {
        Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector());

        if (direction.length() > 0) {
            direction.normalize();
        } else {
            direction = player.getLocation().getDirection();
        }

        direction.multiply(force).setY(yForce);
        target.setVelocity(direction);
    }

    public void drawCircularLine(Location loc, Particle particle, double radius, boolean animated, @Nullable Integer points) {
        int finalPoints = (points == null) ? 20 : points;
        if (animated) {
            // 💡 비동기 스레드 안전성을 위해 스캔할 위치를 클론하여 고정 상수로 보존
            final Location fixedLoc = loc.clone();
            new BukkitRunnable() {
                double angle = 0;
                final double step = (2 * Math.PI) / finalPoints;

                @Override
                public void run() {
                    spawnCircularParticle(fixedLoc, particle, radius, angle);
                    angle += step;

                    if (angle >= 2 * Math.PI) this.cancel();
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 1L);
        } else {
            for (int i = 0; i < finalPoints; i++) {
                double angle = i * ((2 * Math.PI) / finalPoints);
                spawnCircularParticle(loc, particle, radius, angle);
            }
        }
    }

    public void drawCircularLine(Location loc, Particle particle, double radius, boolean animated, @Nullable Integer points, Particle.DustOptions dust) {
        int finalPoints = (points == null) ? 20 : points;

        if (animated) {
            final Location fixedLoc = loc.clone();
            new BukkitRunnable() {
                double angle = 0;
                final double step = (2 * Math.PI) / finalPoints;

                @Override
                public void run() {
                    spawnCircularParticle(fixedLoc, particle, radius, angle, dust);
                    angle += step;

                    if (angle >= 2 * Math.PI) this.cancel();
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 1L);
        } else {
            for (int i = 0; i < finalPoints; i++) {
                double angle = i * ((2 * Math.PI) / finalPoints);
                spawnCircularParticle(loc, particle, radius, angle, dust);
            }
        }
    }

    private void spawnCircularParticle(Location center, Particle particle, double radius, double angle) {
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        Location particleLoc = center.clone().add(x, 0.5, z);
        center.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
    }

    private void spawnCircularParticle(Location center, Particle particle, double radius, double angle, Particle.DustOptions dust) {
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        Location particleLoc = center.clone().add(x, 0.5, z);
        center.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0, dust);
    }

    public void drawLine(Location start, Location end, Particle particle, double space, boolean animated, boolean penetrate, float damage, Player shooter) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        final Set<UUID> hitEntities = new HashSet<>();

        if (animated) {
            new BukkitRunnable() {
                double currentDist = 0;

                @Override
                public void run() {
                    Location point = start.clone().add(direction.clone().multiply(currentDist));

                    if (processLineLogic(point, particle, hitEntities, damage, shooter, penetrate)) {
                        this.cancel();
                        return;
                    }

                    currentDist += space;
                    if (currentDist >= distance) this.cancel();
                }
            }.runTaskTimer(plugin, 0L, 1L); // 💡 엔티티 피해 판단 및 블록 체크가 섞여 있으므로 동기(Sync) 타스크 처리 안전 보장
        } else {
            for (double d = 0; d < distance; d += space) {
                Location point = start.clone().add(direction.clone().multiply(d));
                if (processLineLogic(point, particle, hitEntities, damage, shooter, penetrate)) break;
            }
        }
    }

    private boolean processLineLogic(Location point, Particle particle, Set<UUID> hitEntities, float damage, Player shooter, boolean penetrate) {
        point.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);

        if (damage > 0) {
            if (checkDamage(point, hitEntities, damage, shooter, penetrate)) return true;
        }

        return point.getBlock().getType().isSolid() && !penetrate;
    }

    private boolean checkDamage(Location point, Set<UUID> hitEntities, float damage, Player shooter, boolean penetrate) {
        for (Entity entity : point.getWorld().getNearbyEntities(point, 0.5, 0.5, 0.5)) {
            if (entity instanceof LivingEntity target && !entity.equals(shooter)) {
                if (hitEntities.contains(target.getUniqueId())) continue;

                target.damage(damage, shooter);
                hitEntities.add(target.getUniqueId());

                if (!penetrate) return true;
            }
        }
        return false;
    }

    public void followParticle(Player p, Particle particle, double duration) {
        new BukkitRunnable() {
            double elapsed = 0;
            final double maxTicks = duration;

            @Override
            public void run() {
                if (elapsed >= maxTicks || !p.isOnline()) {
                    this.cancel();
                    return;
                }

                p.getWorld().spawnParticle(particle, p.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);
                elapsed++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void followParticle(Player p, Particle particle, double duration, Particle.DustOptions dust) {
        new BukkitRunnable() {
            double elapsed = 0;
            final double maxTicks = duration;

            @Override
            public void run() {
                if (elapsed >= maxTicks || !p.isOnline()) {
                    this.cancel();
                    return;
                }

                // 💡 비동기 스레드에서 다이렉트로 p.getWorld() 및 p.getLocation() 접근을 회피하기 위해 메인 스레드 스케줄 체인징 사용
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (p.isOnline()) {
                        p.getWorld().spawnParticle(particle, p.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01, dust);
                    }
                });

                elapsed++;
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    public void castSkill(Player p) {
        Location startLoc = p.getLocation();
        Vector direction = startLoc.getDirection().setY(0).normalize();

        new BukkitRunnable() {
            private int count = 1;

            @Override
            public void run() {
                if (!p.isOnline()) {
                    this.cancel();
                    return;
                }

                int maxStrikes = 7;
                if (count > maxStrikes) {
                    this.cancel();
                    return;
                }

                double interval = 2.5;
                double distance = count * interval;
                Vector offset = direction.clone().multiply(distance);
                Location targetLoc = startLoc.clone().add(offset);

                World world = targetLoc.getWorld();
                if (world != null) {
                    Location groundLoc = world.getHighestBlockAt(targetLoc).getLocation();

                    world.strikeLightning(groundLoc);
                }

                count++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public static void startHakiWave(XmasLegacy plugin, @NotNull Location loc) {
        startHakiWave(plugin, loc, "haki_wave");
    }

    public static void startHakiWave(XmasLegacy plugin, @NotNull Location loc, String model) {
        var haki = OraxenItems.getItemById(model);
        if (haki == null) {
            plugin.getSLF4JLogger().error("Oraxen id is not Correct! : \"{}\"", model);
            return;
        }
        ItemStack hakiWave = haki.build();

        Location spawnLoc = loc.clone().add(0, 1.5, 0);
        spawnLoc.setPitch(0.0f);

        ItemDisplay display = spawnLoc.getWorld().spawn(spawnLoc, ItemDisplay.class, w -> {
            w.setItemStack(hakiWave);
            w.setBrightness(new Display.Brightness(15, 15));
            w.setBillboard(Display.Billboard.FIXED);

            Transformation init = w.getTransformation();
            init.getScale().set(0.0f, 0.0f, 0.0f);
            w.setTransformation(init);
        });

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!display.isValid()) return;

            display.setInterpolationDelay(0);
            display.setInterpolationDuration(6);

            Transformation targetTrans = display.getTransformation();
            targetTrans.getScale().set(15.0f, 1.0f, 15.0f);
            display.setTransformation(targetTrans);
        }, 1L);

        Bukkit.getScheduler().runTaskLater(plugin, display::remove, 9L);
    }
}