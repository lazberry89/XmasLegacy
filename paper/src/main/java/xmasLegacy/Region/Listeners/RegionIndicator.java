package xmasLegacy.Region.Listeners;

import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.InfoLevel;
import xmasLegacy.Listeners;
import xmasLegacy.Region.Events.RegionDeleteEvent;
import xmasLegacy.Region.Events.RegionGenerateEvent;
import xmasLegacy.Region.Gui.RegionCreateInterface;
import xmasLegacy.Region.Gui.RegionSettingInterface;
import xmasLegacy.Region.RegionManager;
import xmasLegacy.ServerTransfer;
import xmasLegacy.XmasLegacy;

@SuppressWarnings("DuplicatedCode")
@Listeners
public class RegionIndicator implements Listener {
	private final RegionManager rm;
	private final UserManager um;
	private final XmasLegacy plugin;

	public RegionIndicator() {
		this.rm = RegionManager.getInstance();
		this.um = UserManager.getInstance();
		this.plugin = XmasLegacy.getInstance();
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
			plugin.infoMsg(InfoLevel.ERROR, p, "이미 누군가의 구역입니다!");
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

		if (!target.getPersistentDataContainer().has(plugin.getNamespacedKey(Constants.regionKey))) return;
		p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.0f);
		p.openInventory(new RegionSettingInterface(region).getInventory());
	}
}
