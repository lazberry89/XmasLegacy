package org.lazberry.xmaslegacy.enchant.transcendence.ability.logic;

import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.Ability;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityType;
import org.lazberry.xmaslegacy.utils.*;

import java.util.Collection;
import java.util.Objects;

public class FlameWeaver extends Ability {
	public static final NamespacedKey fireBall = KeyUtils.get("fireball");

	public FlameWeaver(EnchantManager enchantManager, XmasLegacy plugin) {
		super(enchantManager, plugin, AbilityType.FLAME_WEAVER);
	}

	@Override
	public ItemStack weapon() {
		var builder = OraxenItems.getItemById("ignis_blade");
		var item = builder == null ? new ItemStack(getAbilityType().getTranscendenceMaterial()) : builder.build();
		getEnchantManager().setEnchantable(item);
		getEnchantManager().applyLevelChange(item, 10);
		var result = ItemBuilder.of(getPlugin(), item)
				.name(ColorUtils.chat("&#F63302&lI&#F74402&lg&#F85602&ln&#F96701&li" +
						"&#FA7801&ls&#FB8A01&lB&#FC9B01&ll&#FDAC00&la&#FEBE00&ld&#FFCF00&le"))
				.lore(
						EnchantManager.PRISM,
						ColorUtils.chat("&7초월: FlameWeaver")
				)
				.setGlint(true)
				.build();
		KeyUtils.set(result, key, AbilityType.FLAME_WEAVER.name());
		return result;
	}

	@Override
	public boolean execute(Player p) {
		if (p.isFlying()) return false;
		startFlying(p);
		return true;
	}

	private boolean isTarget(Player p, LivingEntity entity) {
		return !Objects.equals(entity.getUniqueId(), p.getUniqueId()) &&
				entity.getFireTicks() > 0;
	}

	private void startFlying(Player p) {
		p.setAllowFlight(true);
		p.setFlying(true);

		var vector = p.getLocation().getDirection().normalize();
		vector.multiply(2);
		vector.setY(2.5f);
		p.setVelocity(vector);

		var world = p.getWorld();
		new BukkitRunnable() {
			int ticks = 60;

			@Override
			public void run() {
				if (p.isDead() || !p.isValid()) {
					p.setAllowFlight(p.getGameMode() == GameMode.CREATIVE);
					p.setFlying(false);
					this.cancel();
					return;
				}
				p.setAllowFlight(true);
				p.setFlying(true);

				var reuse = new Location(world, 0, 0, 0);
				var loc = p.getLocation(reuse);

				if (ticks % 20 == 0) {
					int seconds = ticks/20;
					p.sendActionBar(ColorUtils.chat("&6화염 일격까지: " + seconds + "초"));
				}

				if (ticks <= 0) {
					this.cancel();
					var targets = loc.getNearbyLivingEntities(20, 20, 20,
							l -> isTarget(p, l));
					applyRangeEffect(p, targets);

					p.setAllowFlight(p.getGameMode() == GameMode.CREATIVE);
					p.setFlying(false);
					return;
				}

				world.spawnParticle(Particle.FLAME, loc,
						7, 0.7, 0.7, 0.7, 0.01);
				ticks -= 2;
			}
		}.runTaskTimer(getPlugin(), 0L, 2L);
	}

	private void applyRangeEffect(Player attacker, Collection<LivingEntity> targets) {
		if (targets.isEmpty()) {
			attacker.sendActionBar(ColorUtils.chat("&c타켓 탐색 실패!"));
			attacker.playSound(attacker, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
			attacker.damage(5, attacker);
			return;
		}
		var world = attacker.getWorld();
		SkillEffectManager.drawCircularLine(attacker.getLocation(), Particle.FLAME, 10, false, 200);

		int i = 0;
		for (LivingEntity target : targets) {
			if (!target.isValid() || target.isDead()) continue;

			Location loc = target.getLocation();
			StunUtils.stun(target.getUniqueId(), 20L, "화염 일격");

			int index = i;
			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
				world.createExplosion(loc, 0.5f, false, false, attacker);
				world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0.01);
				world.spawnParticle(Particle.FLAME, loc, 20, 0.8, 0.8, 0.8, 0.01);
			}, index * 5L);
			i++;
		}
	}

	@Override
	public boolean projectile(Player p) {
		shootFireball(p);
		return true;
	}

	public void whenHit(ProjectileHitEvent e) {
		var projectile = e.getEntity();
		if (KeyUtils.hasKey(projectile, fireBall)) {
			var block = e.getHitBlock();
			var entity = e.getHitEntity();

			if (block != null) block.getWorld().createExplosion(block.getLocation(),
					1.0f, false, false);
			else if (entity instanceof LivingEntity target) {
				target.damage(10.0f);
				target.setFireTicks(20 * 5);
				var world = target.getWorld();
				var loc = target.getLocation();

				world.spawnParticle(Particle.EXPLOSION_EMITTER, loc,
						1, 0, 0, 0, 0.01);
				world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
			}
		}
	}

	private void shootFireball(Player p) {
		var vector = p.getEyeLocation().getDirection().normalize();
		var world = p.getWorld();
		vector.multiply(1.5f);

		p.launchProjectile(LargeFireball.class, vector, f -> {
			Location reuse = new Location(world, 0, 0, 0);
			KeyUtils.set(f, fireBall, (byte) 1);
			GlowUtils.glow(f, NamedTextColor.RED);
			f.setShooter(p);
			new BukkitRunnable() {
				@Override
				public void run() {
					if (!f.isValid() || f.isDead()) {
						this.cancel();
						return;
					}
					var loc = f.getLocation(reuse);
					loc.getWorld().spawnParticle(Particle.FLAME, loc, 7, 0.5, 0.5, 0.5, 0.01);
				}
			}.runTaskTimer(getPlugin(), 0L, 2L);
		});
		p.playSound(p, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.0f);
	}
}
