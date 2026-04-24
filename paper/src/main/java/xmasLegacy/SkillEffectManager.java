package xmasLegacy;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class SkillEffectManager {
    private final XmasLegacy plugin;

    public SkillEffectManager(XmasLegacy plugin) {
        this.plugin = plugin;
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
    /**
     * @param loc show Location
     * @param particle particle
     * @param radius 범위
     * @param animated 애니메이션
     * @param points 파티클 갯수
     */
    public void drawCircularLine(Location loc, Particle particle, double radius, boolean animated, @Nullable Integer points) {
        int finalPoints = (points == null) ? 20 : points; // 기본값 20 설정
        if (animated) {
            new BukkitRunnable() {
                double angle = 0;
                final double step = (2 * Math.PI) / finalPoints;

                @Override
                public void run() {
                    spawnCircularParticle(loc, particle, radius, angle);
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
		int finalPoints = (points == null) ? 20 : points; // 기본값 20 설정

		if (animated) {
			new BukkitRunnable() {
				double angle = 0;
				final double step = (2 * Math.PI) / finalPoints;

				@Override
				public void run() {
					spawnCircularParticle(loc, particle, radius, angle, dust);
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

    /**
     *
     * @param start 시작지점
     * @param end 끝지점
     * @param particle 파티클
     * @param space 파티클 사이 거리
     * @param animated 애니메이션
     * @param penetrate 관통여부
     * @param damage 대미지
     * @param shooter 공격자
     */
    public void drawLine(Location start, Location end, Particle particle, double space, boolean animated, boolean penetrate, float damage, Player shooter) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        final Set<UUID> hitEntities = new HashSet<>(); // 애니메이션을 위해 final 선언

        if (animated) {
            new BukkitRunnable() {
                double currentDist = 0;

                @Override
                public void run() {
                    Location point = start.clone().add(direction.clone().multiply(currentDist));

                    // 파티클 소환 및 로직 체크
                    if (processLineLogic(point, particle, hitEntities, damage, shooter, penetrate)) {
                        this.cancel();
                        return;
                    }

                    currentDist += space;
                    if (currentDist >= distance) this.cancel();
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 1L);
        } else {
            for (double d = 0; d < distance; d += space) {
                Location point = start.clone().add(direction.clone().multiply(d));
                if (processLineLogic(point, particle, hitEntities, damage, shooter, penetrate)) break;
            }
        }
    }

    /**
     *
     * @param point 위치
     * @param particle 파티클
     * @param hitEntities 격피 개체
     * @param damage 대미지
     * @param shooter 공격자
     * @param penetrate 관토여부
     * @return true/false
     */
    private boolean processLineLogic(Location point, Particle particle, Set<UUID> hitEntities, float damage, Player shooter, boolean penetrate) {
        // 1. 파티클 소환
        point.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);

        // 2. 대미지 체크
        if (damage > 0) {
            if (checkDamage(point, hitEntities, damage, shooter, penetrate)) return true;
        }

        // 3. 벽 체크
        if (point.getBlock().getType().isSolid() && !penetrate) return true;

        return false;
    }

    private boolean checkDamage(Location point, Set<UUID> hitEntities, float damage, Player shooter, boolean penetrate) {
        for (Entity entity : point.getWorld().getNearbyEntities(point, 0.5, 0.5, 0.5)) {
            if (entity instanceof LivingEntity target && !entity.equals(shooter)) {
                if (hitEntities.contains(target.getUniqueId())) continue;

                target.damage(damage, shooter);
                hitEntities.add(target.getUniqueId());

                if (!penetrate) return true; // 관통 불가 시 즉시 중단 신호
            }
        }
        return false;
    }
	public void followParticle(Player p, Particle particle, double duration) {
		new BukkitRunnable() {
			// 1틱마다 실행 (0.05초)
			double elapsed = 0;
			final double maxTicks = duration * 20;

			@Override
			public void run() {
				// 1. 시간이 다 됐거나 플레이어가 나갔으면 종료
				if (elapsed >= maxTicks || !p.isOnline()) {
					this.cancel();
					return;
				}

				// 2. 파티클 소환
				p.getWorld().spawnParticle(particle, p.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);

				// 3. 카운트 증가
				elapsed++;
			}
		}.runTaskTimer(plugin, 0L, 1L); // 1L(1틱) 간격으로 무지하게 부드럽게 출력
	}
	public void followParticle(Player p, Particle particle, double duration, Particle.DustOptions dust) {
		new BukkitRunnable() {

			double elapsed = 0;

			final double maxTicks = duration * 20;

			@Override
			public void run() {
				// 1. 시간이 다 됐거나 플레이어가 나갔으면 종료
				if (elapsed >= maxTicks || !p.isOnline()) {
					this.cancel();
					return;
				}

				// 2. 파티클 소환
				p.getWorld().spawnParticle(particle, p.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01, dust);

				// 3. 카운트 증가
				elapsed++;
			}
		}.runTaskTimerAsynchronously(plugin, 0L, 1L); // 1L(1틱) 간격으로 무지하게 부드럽게 출력
	}
}