package org.lazberry.xmaslegacy.enchant.transcendence.ability.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.Ability;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityManager;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityType;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.logic.AeroMancer;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class AeroMancerListener implements Listener {
	private final AeroMancer aeroMancer;
	private final XmasLegacy plugin;

	@Inject
	public AeroMancerListener(AbilityManager abilityManager, XmasLegacy plugin) {
		this.aeroMancer = abilityManager.getAbility(AbilityType.AERO_MANCER);
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;

		var item = p.getInventory().getItemInMainHand();
		if (!Ability.isAbilityWeapon(item, AbilityType.AERO_MANCER)) return;

		if (p.isOnGround() && !p.getAllowFlight()) {
			p.setAllowFlight(true);
		}
	}

	@EventHandler
	public void onToggleFlight(PlayerToggleFlightEvent e) {
		Player p = e.getPlayer();

		if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;
		var item = p.getInventory().getItemInMainHand();

		e.setCancelled(true);
		p.setFlying(false);

		p.setAllowFlight(false);

		if (!Ability.isAbilityWeapon(item, AbilityType.AERO_MANCER)) return;
		Vector velocity = p.getLocation().getDirection().multiply(1).setY(0.7);
		p.setVelocity(velocity);

		Location loc = p.getLocation();
		loc.getWorld().spawnParticle(Particle.CLOUD, loc, 15, 0.3, 0.1, 0.3, 0.05);
		loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7f, 1.5f);
	}
}
