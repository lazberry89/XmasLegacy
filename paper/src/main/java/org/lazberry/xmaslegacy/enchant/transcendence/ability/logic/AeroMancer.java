package org.lazberry.xmaslegacy.enchant.transcendence.ability.logic;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.Ability;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.Objects;

public class AeroMancer extends Ability {

	public AeroMancer(EnchantManager enchantManager, XmasLegacy plugin) {
		super(enchantManager, plugin, AbilityType.AERO_MANCER);
	}

	@Override
	public ItemStack weapon() {
		var builder = OraxenItems.getItemById("sky_reaver");
		var item = builder == null ? new ItemStack(getAbilityType().getTranscendenceMaterial()) : builder.build();

		getEnchantManager().setEnchantable(item);
		getEnchantManager().applyLevelChange(item, 10);
		var result = ItemBuilder.of(getPlugin(), item)
				.name(ColorUtils.chat("&#56F8FF&lS&#6BF9FF&lk&#80FAFF&ly&#95FBFF&lR&#ABFCFF" +
						"&le&#C0FCFF&la&#D5FDFF&lv&#EAFEFF&le&#FFFFFF&lr"))
				.lore(
						EnchantManager.PRISM,
						ColorUtils.chat("&7초월: AeroMancer")
				)
				.setGlint(true)
				.build();
		KeyUtils.set(result, key, getAbilityType().name());
		return result;
	}

	@Override
	public boolean execute(Player p) {
		executeDash(p);
		return true;
	}

	private void executeDash(Player executor) {
		var loc = executor.getLocation();
		loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

		var vector = loc.getDirection().normalize();
		vector.multiply(2.5);
		vector.setY(0.3);

		executor.setVelocity(vector);
		Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
			dashImpact(executor), 10L);
	}

	private void dashImpact(Player p) {
		var loc = p.getLocation();
		var world = loc.getWorld();
		var vector = loc.getDirection().setY(0).normalize().multiply(5);

		Location effectLoc = loc.clone().add(vector);

		world.playSound(effectLoc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.0f);
		world.spawnParticle(Particle.EXPLOSION_EMITTER, effectLoc, 1, 0, 0, 0, 0.01);

		var list = effectLoc.getNearbyLivingEntities(5, 5, 5,
				l -> !Objects.equals(l.getUniqueId(), p.getUniqueId()));
		if (list.isEmpty()) return;
		list.forEach(target -> {
			knockbackEntity(p, target);
			target.damage(7, DamageSource.builder(DamageType.EXPLOSION)
					.withCausingEntity(p)
					.withDirectEntity(target)
					.build());
		});
	}

	private void knockbackEntity(Player player, LivingEntity target) {
		Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector());

		if (direction.length() > 0) direction.normalize();
		else direction = player.getLocation().getDirection();

		direction.multiply(1.5).setY(0.4);
		target.setVelocity(direction);
	}

	@Override
	public boolean projectile(Player p) {
		shootAirBullet(p);
		return true;
	}

	private void shootAirBullet(Player p) {
		Location init = p.getLocation();
		World world = p.getWorld();

		var vector = init.getDirection().normalize();

		for (int i = 1; i < 5; i++) {
			int step = i;
			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
				var effectLoc = init.clone().add(vector.clone().multiply(5 * step));
				world.createExplosion(effectLoc, 2.0f, false, false, p);
				world.spawnParticle(Particle.EXPLOSION_EMITTER, effectLoc, 1, 0, 0, 0, 0.01);
				world.playSound(effectLoc, Sound.ENTITY_BREEZE_WIND_BURST, 1.0f, 1.0f);
			}, step * 5L);
		}
	}
}
