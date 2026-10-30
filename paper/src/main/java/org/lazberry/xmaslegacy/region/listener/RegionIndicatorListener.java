package org.lazberry.xmaslegacy.region.listener;

import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.region.RegionManager;
import org.lazberry.xmaslegacy.region.event.RegionDeleteEvent;
import org.lazberry.xmaslegacy.region.event.RegionGenerateEvent;
import org.lazberry.xmaslegacy.region.indicator.RegionDisplayManager;
import org.lazberry.xmaslegacy.region.screen.RegionCreateInterface;
import org.lazberry.xmaslegacy.region.screen.RegionSettingInterface;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.utils.UserHandler;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class RegionIndicatorListener implements Listener {
	private final RegionDisplayManager displayManager;
	private final RegionManager rm;
	private final UserManager um;

	@Inject
	public RegionIndicatorListener(RegionDisplayManager displayManager, UserManager um, RegionManager rm) {
		this.displayManager = displayManager;
		this.rm = rm;
		this.um = um;
	}

	@EventHandler
	public void UserRegionCreate(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		if (item == null || !rm.isTicket(item)) return;

		var user = um.getUser(p.getUniqueId());
		var loc = p.getLocation();
		if (user == null) {
			UserHandler.loadUser(p, false);
			return;
		}
		if (rm.hasRegion(loc)) {
			e.setCancelled(true);
			InfoUtils.error(p, "이미 누군가의 구역입니다!");
			return;
		}
		p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
		p.openInventory(new RegionCreateInterface().getInventory());
	}

	@EventHandler
	public void MakeRegionIndicator(RegionGenerateEvent e) {
		var region = e.getRegion();
		if (region == null || !region.isValid()) return;

		Chunk chunk = region.getChunk();
		if (chunk != null && !chunk.isLoaded()) {
			chunk.load(true);
		}
		displayManager.spawnIndicatorDisplay(region);
	}

	@EventHandler
	public void removeWhenDeleted(RegionDeleteEvent e) {
		var region = e.getRegion();
		if (region != null) {
			displayManager.removeDisplay(region);
		}
	}

	@EventHandler
	public void RegionIndicatorClick(PlayerInteractAtEntityEvent e) {
		Player p = e.getPlayer();
		Entity target = e.getRightClicked();

		var region = rm.getRegionAt(p.getLocation());
		if (region == null) return;
		if (!p.getUniqueId().equals(region.getOwner())) return;

		if (!target.getPersistentDataContainer().has(KeyUtils.get(Constants.regionKey))) return;
		p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.0f);
		p.openInventory(new RegionSettingInterface(region).getInventory());
	}
}
