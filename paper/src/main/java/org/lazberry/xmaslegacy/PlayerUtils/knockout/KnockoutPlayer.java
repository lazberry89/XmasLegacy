package org.lazberry.xmaslegacy.PlayerUtils.knockout;

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
import org.lazberry.xmaslegacy.utils.StunUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
public class KnockoutPlayer {
	public static final NamespacedKey key = KeyUtils.get("knockdown");
	public static final NamespacedKey left = KeyUtils.get("left_knocked");

	public static boolean isKnockedOut(Player player) {
		return player.getPersistentDataContainer().has(key);
	}

	private final Collection<String> completions = List.of("살려주세요!", "기절했어요, 도와주세요!", "여기와서 좀 도와줘!");
	private final UUID uuid;
	private final double reviveHealth;
	private int reviveCount;
	private long lastClickedMillis = 0;

	public KnockoutPlayer(Player player, int reviveCount, double reviveHealth) {
		this.uuid = player.getUniqueId();
		this.reviveHealth = reviveHealth;
		this.reviveCount = reviveCount;
		knockdown(player);
	}

	public Optional<Player> getPlayer() {
		return Optional.ofNullable(Bukkit.getPlayer(uuid));
	}

	private void knockdown(Player player) {
		var healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
		if (healthAttribute != null) {
			double value = healthAttribute.getValue();
			player.setHealth(value);
		}
		GlowUtils.glow(player, NamedTextColor.WHITE);
		KeyUtils.set(player, key, true);
		StunUtils.stun(player.getUniqueId(), "기절");
		player.setPose(Pose.SLEEPING, true);
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.DARKNESS, Integer.MAX_VALUE, 2, true, false, false));
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.WITHER, Integer.MAX_VALUE, 2, true, false, false));

		player.addCustomChatCompletions(completions);
	}

	public void revive() {
		OptionalUtils.ifNotNull(Bukkit.getPlayer(uuid), player -> {
			if (!player.isValid() || !player.isOnline()) return;

			cleanup();

			player.setHealth(reviveHealth);

			var world = player.getWorld();
			var loc = player.getLocation();
			world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 10, 0.5, 0.5, 0.5, 0.01);
			world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
		});
	}

	public void cleanup() {
		OptionalUtils.ifNotNull(Bukkit.getPlayer(uuid), player -> {
			GlowUtils.clearGlow(player);
			StunUtils.release(player.getUniqueId());
			player.removeCustomChatCompletions(completions);

			if (player.getPersistentDataContainer().has(key)) {
				player.getPersistentDataContainer().remove(key);
			}

			player.removePotionEffect(PotionEffectType.DARKNESS);
			player.removePotionEffect(PotionEffectType.WITHER);
			player.setPose(Pose.STANDING, true);
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
