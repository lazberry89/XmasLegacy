package xmaslegacy.Region.Listeners;

import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Annotation.Listeners;
import xmaslegacy.Region.Events.RegionDeleteEvent;
import xmaslegacy.Region.Events.RegionGenerateEvent;
import xmaslegacy.Region.Gui.RegionCreateInterface;
import xmaslegacy.Region.Gui.RegionSettingInterface;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.KeyUtils;
import xmaslegacy.Utils.ServerTransfer;

@Listeners
public class RegionIndicator implements Listener {
	private final @NotNull RegionManager rm;
	private final @NotNull UserManager um;

	public RegionIndicator() {
		this.rm = RegionManager.INSTANCE;
		this.um = UserManager.INSTANCE;
	}

	@EventHandler
	public void UserRegionCreate(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItemDrop().getItemStack();
		if (!rm.isTicket(item)) return;

		var user = um.getUser(p.getUniqueId());
		var loc = p.getLocation();
		if (user == null) {
			ServerTransfer.loadUser(p, false);
			return;
		}
		if (rm.hasRegion(loc)) {
			e.setCancelled(true);
			InfoUtils.error(p, "이미 누군가의 구역입니다!");
			return;
		}
		p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
		p.openInventory(new RegionCreateInterface().getInventory());
		e.getItemDrop().remove();
	}

	@EventHandler
	public void MakeRegionIndicator(RegionGenerateEvent e) {
		var region = e.getRegion();

		Chunk chunk = region.getChunk();
		if (!region.isValid()) return;
		if (chunk == null) return;

		region.setIndicator(rm.indicatorDisplay(region));
	}

	@EventHandler
	public void removeWhenDeleted(RegionDeleteEvent e) {
		var chunk = e.getRegion().getChunk();
		if (chunk == null) return;

		var region = e.getRegion();
		var indic = region.getIndicator();
		if (indic != null && indic.isValid()) indic.remove();
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
