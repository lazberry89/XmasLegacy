package xmasLegacy.Region;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@SuppressWarnings("ClassCanBeRecord")
public class RegionPermission implements Listener {
	private final RegionManager RM;

	public RegionPermission(RegionManager RM) {
		this.RM = RM;
	}

	// 권한이 있는지 확인하는 공통 메서드
	private boolean hasPermission(Player p, Region region) {
		return p.isOp() || region.getOwner().equals(p.getUniqueId());
	}

	@EventHandler
	public void regionEnterEvent(PlayerMoveEvent e) {
		Region region = RM.getRegionAt(e.getTo());
		if (region == null) return;

		if (region.isInsideSafeZone(e.getTo())) {
			Player p = e.getPlayer();
			if (hasPermission(p, region)) return;

			if (!region.isAllowPublicEntry()) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) return;
		Region region = RM.getRegionAt(e.getClickedBlock().getLocation());
		if (region == null || hasPermission(e.getPlayer(), region)) return;

		if (!region.isAllowPublicInteraction()) {
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
		Region region = RM.getRegionAt(target.getLocation());
		if (region == null || hasPermission(p, region)) return;

		if (!region.isAllowPublicInteraction()) {
			event.setCancelled(true);
		}
	}
}
