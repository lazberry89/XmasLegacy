package org.lazberry.xmaslegacy.Region.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Listeners;
import org.lazberry.xmaslegacy.Region.Region;
import org.lazberry.xmaslegacy.Region.RegionManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;

@Inject
@Listeners
public class RegionPermissionListener implements Listener {
	private @NotNull RegionManager rm;

	public RegionPermissionListener() {}

	private boolean hasPermission(Player p, Region region) {
		return p.isOp() || region.getOwner().equals(p.getUniqueId());
	}

	@EventHandler
	public void regionEnterEvent(PlayerMoveEvent e) {
		Region region = rm.getRegionAt(e.getTo());
		if (region == null) return;

		if (region.isInside(e.getTo())) {
			Player p = e.getPlayer();
			if (hasPermission(p, region)) return;

			if (!region.isEntryAllowed()) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) return;
		Region region = rm.getRegionAt(e.getClickedBlock().getLocation());
		if (region == null || hasPermission(e.getPlayer(), region)) return;

		if (!region.isInteractionAllowed()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		handleEntityInteraction(e.getPlayer(), e.getRightClicked(), e);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player p) {
			handleEntityInteraction(p, e.getEntity(), e);
		}
	}

	private void handleEntityInteraction(Player p, Entity target, org.bukkit.event.Cancellable event) {
		Region region = rm.getRegionAt(target.getLocation());
		if (region == null || hasPermission(p, region)) return;

		if (!region.isInteractionAllowed()) {
			event.setCancelled(true);
		}
	}
}
