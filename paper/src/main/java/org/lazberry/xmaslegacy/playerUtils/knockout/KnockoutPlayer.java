package org.lazberry.xmaslegacy.playerUtils.knockout;

import lombok.Data;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;

import java.util.Optional;
import java.util.UUID;

@Data
public class KnockoutPlayer {
	public static final NamespacedKey key = KeyUtils.get("knockdown");

	private final UUID uuid;
	private final double reviveHealth;
	private int reviveCount;
	private long lastClickedMillis = 0;

	public static boolean isKnockedOut(Player player) {
		return player.getPersistentDataContainer().has(key);
	}

	public Optional<Player> getPlayer() {
		return Optional.ofNullable(Bukkit.getPlayer(uuid));
	}

	protected KnockoutPlayer(Player player, int reviveCount, double reviveHealth) {
		this.uuid = player.getUniqueId();
		this.reviveHealth = reviveHealth;
		this.reviveCount = reviveCount;
		knockdown(player);
	}

	private void knockdown(Player player) {
		var healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
		if (healthAttribute != null) {
			double value = healthAttribute.getValue();
			player.setHealth(value);
		}
		GlowUtils.glow(player, NamedTextColor.WHITE);
		KeyUtils.set(player, key, true);
		player.setPose(Pose.SWIMMING, true);
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 2, true, false, false));
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.DARKNESS, Integer.MAX_VALUE, 2, true, false, false));
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.WITHER, Integer.MAX_VALUE, 1, true, false, false));
	}

	public void revive() {
		OptionalUtils.ifNotNull(Bukkit.getPlayer(uuid), player -> {
			if (!player.isValid() || !player.isOnline()) return;

			var world = player.getWorld();
			var loc = player.getLocation();
			GlowUtils.clearGlow(player);
			if (player.getPersistentDataContainer().has(key))
				player.getPersistentDataContainer().remove(key);
			player.removePotionEffect(PotionEffectType.SLOWNESS);
			player.removePotionEffect(PotionEffectType.DARKNESS);
			player.removePotionEffect(PotionEffectType.WITHER);
			player.setPose(Pose.STANDING, false);

			player.setHealth(reviveHealth);
			player.setPose(Pose.STANDING, true);
			world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 10, 0.5, 0.5, 0.5, 0.01);
			world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
		});
	}

	public State touch() {
		if (System.currentTimeMillis() - lastClickedMillis <= 1000) return State.TOO_FAST;
		lastClickedMillis = System.currentTimeMillis();
		if (--reviveCount <= 0) {
			revive();
			return State.REVIVED;
		}
		return State.PROGRESS;
	}

	public enum State {
		TOO_FAST,
		PROGRESS,
		REVIVED
	}
}
