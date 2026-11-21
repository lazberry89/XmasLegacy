package org.lazberry.xmaslegacy.damage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.util.concurrent.ThreadLocalRandom;

/** 플레이어가 입힌 대미지를 맞은 엔티티 위에 떠오르는 숫자로 표시한다. */
@Listeners
@Components.Exclude(type = ServerType.LOBBY)
public class DamageIndicatorListener implements Listener {
	private static final int LIFE_TICKS = 24;
	private static final float RISE = 1.0f;
	private final XmasLegacy plugin;

	@Inject
	public DamageIndicatorListener(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof LivingEntity target) || target instanceof ArmorStand) return;
		if (!(attacker(e) instanceof Player)) return;

		double damage = e.getFinalDamage();
		if (damage <= 0) return;

		spawn(target, damage, e.isCritical());
	}

	private Object attacker(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Projectile proj) return proj.getShooter();
		return e.getDamager();
	}

	private void spawn(LivingEntity target, double damage, boolean crit) {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		Location loc = target.getLocation().add(
				r.nextDouble(-0.4, 0.4), target.getHeight() + 0.2, r.nextDouble(-0.4, 0.4));

		Component text = Component.text((crit ? "✦ " : "") + String.format("%.1f", damage))
				.color(crit ? NamedTextColor.GOLD : NamedTextColor.RED)
				.decoration(TextDecoration.BOLD, crit);

		float scale = crit ? 1.3f : 1.0f;
		TextDisplay display = loc.getWorld().spawn(loc, TextDisplay.class, d -> {
			d.setPersistent(false);
			d.text(text);
			d.setBillboard(Display.Billboard.CENTER);
			d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
			d.setShadowed(true);
			d.setSeeThrough(true);
			d.setTransformation(transform(0f, scale));
		});

		burst(loc, crit);
		new BukkitRunnable() {
			int tick = 0;

			@Override
			public void run() {
				if (!display.isValid() || ++tick > LIFE_TICKS) {
					display.remove();
					cancel();
					return;
				}
				float t = tick / (float) LIFE_TICKS;
				float rise = RISE * (1 - (1 - t) * (1 - t)); // ease-out
				float sway = (float) Math.sin(tick * 0.6) * 0.04f;

				// 마지막 40%: 가로로 가늘어지고 세로로 늘어나며 옅어진다(증발)
				float fade = Math.max(0f, (t - 0.6f) / 0.4f);
				display.setInterpolationDelay(0);
				display.setInterpolationDuration(1);
				display.setTransformation(transform(sway, rise,
						scale * (1 - 0.6f * fade), scale * (1 + 0.5f * fade)));
				// 26 미만은 클라이언트가 불투명으로 그리므로 26 이상만 사용
				display.setTextOpacity((byte) (255 - (int) (229 * fade)));

				if (tick % 2 == 0) {
					Location p = loc.clone().add(sway, rise + 0.15, 0);
					p.getWorld().spawnParticle(fade > 0 ? Particle.CLOUD : Particle.WHITE_ASH,
							p, fade > 0 ? 2 : 3, 0.12, 0.05, 0.12, 0.01);
				}
			}
		}.runTaskTimer(plugin, 1L, 1L);
	}

	private static void burst(Location loc, boolean crit) {
		loc.getWorld().spawnParticle(Particle.CRIT, loc, crit ? 14 : 6, 0.2, 0.15, 0.2, 0.25);
		if (crit) loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 10, 0.25, 0.2, 0.25, 0.3);
	}

	private static Transformation transform(float y, float scale) {
		return transform(0, y, scale, scale);
	}

	private static Transformation transform(float x, float y, float sx, float sy) {
		return new Transformation(new Vector3f(x, y, 0), new Quaternionf(),
				new Vector3f(sx, sy, sx), new Quaternionf());
	}
}
