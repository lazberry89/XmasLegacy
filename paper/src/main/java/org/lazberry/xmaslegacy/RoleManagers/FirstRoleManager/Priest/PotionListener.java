package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Priest;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.*;

@Listeners
public class PotionListener implements Listener {
	private final @NotNull XmasLegacy plugin;
	private final @NotNull Set<UUID> deathSaver = new HashSet<>();

	public PotionListener() {
		this.plugin = XmasLegacy.getInstance();
	}

	@EventHandler
	public void onPotionUse(PlayerInteractEvent e) {
		var p = e.getPlayer();
		if (!e.getAction().name().contains("RIGHT_CLICK")) return;
		if (e.getItem() == null) return;

		if (e.getItem().isSimilar(ConductableItems.DragonPotion())) {
			cancelAndConsume(e);
			p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Constants.DRAGON_POTION_DURATION * 20, Constants.DRAGON_HEAL_AMPLIFIER, true, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Constants.DRAGON_POTION_DURATION * 20, Constants.DRAGON_SATURATION_AMPLIFIER, true, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Constants.DRAGON_POTION_DURATION * 20, Constants.DRAGON_PROTECTION_AMPLIFIER, true, false));
			// 드래곤 - 웅장하고 강렬한 느낌
			p.playSound(p, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.2f);
			Particle.DustTransition transition = new Particle.DustTransition(Color.FUCHSIA, Color.WHITE, 1.0f);
			p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, p.getLocation(), 15, 0.3, 0.3, 0.3, 0, transition);

		} else if (e.getItem().isSimilar(ConductableItems.HealerPotion())) {
			cancelAndConsume(e);
			p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Constants.HEALER_POTION_DURATION * 20, Constants.HEALER_POTION_AMPLIFIER, true, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Constants.HEALER_POTION_DURATION * 10, 0, true, false, false));
			// 힐 - 부드럽고 회복되는 느낌
			p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f);
			Particle.DustTransition transition = new Particle.DustTransition(Color.ORANGE, Color.YELLOW, 1.5f);
			p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, p.getLocation(), 15, 0.3, 0.3, 0.3, 0, transition);

		} else if (e.getItem().isSimilar(ConductableItems.ProtectionPotion())) {
			cancelAndConsume(e);
			p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Constants.PROTECTION_POTION_DURATION * 20, Constants.DRAGON_PROTECTION_AMPLIFIER, true, false));
			// 보호막 - 단단하고 방어적인 느낌
			p.playSound(p, Sound.ITEM_SHIELD_BLOCK, 1.0f, 0.8f);
			Particle.DustTransition transition = new Particle.DustTransition(Color.NAVY, Color.BLUE, 1.0f);
			p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, p.getLocation(), 15, 0.3, 0.3, 0.3, 0, transition);

		} else if (e.getItem().isSimilar(ConductableItems.DeathSave())) {
			cancelAndConsume(e);
			deathSaver.add(p.getUniqueId());
			p.playSound(p, Sound.ENTITY_WITHER_SPAWN, 0.4f, 1.5f);
			Particle.DustTransition deathDust = new Particle.DustTransition(Color.BLACK, Color.RED, 1.2f);

			for (int i = 0; i < 8; i++) {
				double angle = i * 0.8;
				double x = Math.cos(angle) * 0.5;
				double z = Math.sin(angle) * 0.5;
				p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
						p.getLocation().add(x, i * 0.3, z), 5, 0.1, 0.1, 0.1, 0, deathDust);
			}

			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if (deathSaver.contains(p.getUniqueId()) && p.isValid()) {
					deathSaver.remove(p.getUniqueId());
					for (int i = 8; i > 0; i--) {
						double angle = i * 0.8;
						double x = Math.cos(angle) * 0.5;
						double z = Math.sin(angle) * 0.5;
						p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
								p.getLocation().add(x, i * 0.3, z), 5, 0.1, 0.1, 0.1, 0, deathDust);
					}
				}
			}, (long) Constants.DEATH_SAVER_DURATION * 20);
		}
	}

	@EventHandler
	public void onPiercePotionUse(PotionSplashEvent e) {
		ThrownPotion potion = e.getPotion();
		if (!(e.getEntity().getShooter() instanceof Player)) return;
		if (!potion.getItem().isSimilar(ConductableItems.SpearPotion())) return;

		for (LivingEntity entity : e.getAffectedEntities()) {
			playSpearEffect(entity);
			AttributeInstance pa = entity.getAttribute(Attribute.MOVEMENT_SPEED);
			if (pa != null) {
				double originalValue = pa.getBaseValue();
				pa.setBaseValue(0);
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					if (entity.isValid()) {
						pa.setBaseValue(originalValue);
					}
				}, 10L);
			}
		}
	}

	private void playSpearEffect(LivingEntity target) {
		Location loc = target.getLocation();
		double entityHeight = target.getHeight();

		Particle.DustOptions shaftColor = new Particle.DustOptions(Color.SILVER, 1.0f);
		Particle.DustOptions tipColor = new Particle.DustOptions(Color.GRAY, 1.3f);

		for (double y = entityHeight + 1.5; y >= 0; y -= 0.1) {
			Location point = loc.clone().add(0, y, 0);
			if (y > 0.4) {
				target.getWorld().spawnParticle(Particle.DUST, point, 2, 0.02, 0.02, 0.02, 0, shaftColor);
			} else {
				target.getWorld().spawnParticle(Particle.DUST, point, 3, 0.05, 0.05, 0.05, 0, tipColor);
			}
		}

		target.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(0, entityHeight / 2, 0), 15, 0.2, 0.5, 0.2, 0.1);
		// 창 관통 - 날카롭고 무거운 충격 느낌
		target.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_HURT, 0.8f, 1.6f);
	}

	@EventHandler
	public void onSaverUse(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player p)) return;
		if (!deathSaver.contains(p.getUniqueId())) return;
		if (p.getHealth() - e.getFinalDamage() > 0) return;
		e.setCancelled(true);
		p.setHealth(8);
		p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Constants.DEATH_SAVER_DURATION * 200, 1, true, false));
		playDeathSaveEffect(p);
		deathSaver.remove(p.getUniqueId());
	}

	private void cancelAndConsume(PlayerInteractEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		if (item == null) return;
		if (p.getCooldown(item) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 사용할 수 없습니다!"));
			e.setCancelled(true);
			return;
		}
		item.setAmount(e.getItem().getAmount() - 1);
		if (item.getAmount() > 0) {
			p.setCooldown(e.getItem(), 40);
		}
	}

	public void playDeathSaveEffect(Player p) {
		Location loc = p.getLocation();
		Particle.DustTransition transition = new Particle.DustTransition(Color.YELLOW, Color.WHITE, 1.5f);

		for (double i = 0; i < 4; i += 0.2) {
			double radius = 0.8;
			double angle = i * 2.5;

			double x = radius * Math.cos(angle);
			double z = radius * Math.sin(angle);
			p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
					loc.clone().add(x, i, z), 5, 0.1, 0.1, 0.1, 0, transition);

			double x2 = radius * Math.cos(angle + Math.PI);
			double z2 = radius * Math.sin(angle + Math.PI);
			p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
					loc.clone().add(x2, i, z2), 5, 0.1, 0.1, 0.1, 0, transition);
		}

		p.getWorld().spawnParticle(Particle.FLASH, loc, 3);
		// 데스세이버 발동 - 토템처럼 극적인 부활 느낌
		p.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
	}
}