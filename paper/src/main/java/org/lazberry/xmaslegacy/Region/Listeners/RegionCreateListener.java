package org.lazberry.xmaslegacy.Region.Listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Listeners;
import org.lazberry.xmaslegacy.Region.Gui.RegionCreateInterface;
import org.lazberry.xmaslegacy.Region.Region;
import org.lazberry.xmaslegacy.Region.RegionManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;

@Listeners
public class RegionCreateListener implements Listener {
	private final @NotNull RegionManager rm;

	public RegionCreateListener() {
		this.rm = RegionManager.INSTANCE;
	}

	@EventHandler
	public void regionInterface(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;

		Inventory inv = e.getClickedInventory();
		if (inv == null) return;

		if (!(inv.getHolder() instanceof RegionCreateInterface)) return;

		e.setCancelled(true);

		int slot = e.getRawSlot();
		if (slot == 4) {
			Region region = new Region(p, p.getLocation());
			if (region.isValid() && !rm.hasRegion(p.getLocation())) {
				rm.addRegion(p, region);
				InfoUtils.info(p, "구역이 성공적으로 생성되었습니다!");
				p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
				p.closeInventory();
			} else {
				InfoUtils.error(p, "구역을 생성할 수 없습니다! 이미 구역이 존재하거나 청크가 생성되지 않았습니다.");
				p.closeInventory();
			}
		}
	}
}
