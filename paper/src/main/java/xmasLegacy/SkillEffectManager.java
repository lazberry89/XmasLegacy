package xmasLegacy;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

@SuppressWarnings("unused")
public class SkillEffectManager {
    private final XmasLegacy plugin;
    private final Map<UUID, Long> stun = new HashMap<>();
    private final Set<UUID> activeStunTimers = new HashSet<>();
    private final Set<LivingEntity> hideMap = new HashSet<>();
    private static SkillEffectManager instance;

    public SkillEffectManager() {
        this.plugin = XmasLegacy.getInstance();
    }

    public static SkillEffectManager getInstance() {
        if (instance == null) instance = new SkillEffectManager();
        return instance;
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
    public Set<LivingEntity> getHiddenEntity() {
        return this.hideMap;
    }
    public void showEntity(LivingEntity le) {
        if (this.hideMap.remove(le)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showEntity(plugin, le);
            }
            if (le instanceof Player player) {
                player.setInvisible(false);
            }
        }
    }


    @Unmodifiable
    public Set<UUID> stunMap() {
        return Collections.unmodifiableSet(this.activeStunTimers);
    }


    public void deStun(UUID uuid) {
        this.activeStunTimers.remove(uuid);
    }

    public void StunEntity(UUID uuid) {
        this.activeStunTimers.add(uuid);
    }

    public void StunEntity(UUID uuid, long period) {
        stun.put(uuid, stun.getOrDefault(uuid, 0L) + period);

        if (activeStunTimers.contains(uuid)) return;
        activeStunTimers.add(uuid);

        new BukkitRunnable() {
            @Override
            public void run() {
                Long current = stun.get(uuid);

                if (current == null || current <= 0L) {
                    stun.remove(uuid);
                    activeStunTimers.remove(uuid);
                    this.cancel();
                    return;
                }

                stun.put(uuid, current - 1);
            }
        }.runTaskTimer(plugin, 0L, 1L);
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
            final double maxTicks = duration * 20;

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
            final double maxTicks = duration * 20;

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

    public static void startHakiWave(XmasLegacy plugin, @NotNull Location loc) {
        var haki = OraxenItems.getItemById("haki_wave");
        if (haki == null) {
            plugin.getSLF4JLogger().error("Oraxen id is not Correct! : \"haki_wave\"");
            return;
        }
        ItemStack hakiWave = haki.build();

        Location spawnLoc = loc.clone().add(0, 1.5, 0);
        spawnLoc.setPitch(0.0f);

        ItemDisplay display = spawnLoc.getWorld().spawn(spawnLoc, ItemDisplay.class, w -> {
            w.setItemStack(hakiWave);
            w.setBrightness(new Display.Brightness(15, 15));
            w.setBillboard(Display.Billboard.FIXED);

            w.setInterpolationDuration(6);
            w.setInterpolationDelay(0);

            Transformation init = w.getTransformation();
            init.getScale().set(1.0f, 1.0f, 1.0f);
            w.setTransformation(init);
        });

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!display.isValid()) return;

            Transformation targetTrans = display.getTransformation();
            targetTrans.getScale().set(15.0f, 1.0f, 15.0f);
            display.setTransformation(targetTrans);
        }, 1L);

        Bukkit.getScheduler().runTaskLater(plugin, display::remove, 9L);
    }
}