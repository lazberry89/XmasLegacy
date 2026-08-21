package org.lazberry.xmaslegacy.region.Listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.region.Gui.RegionCreateInterface;
import org.lazberry.xmaslegacy.region.Region;
import org.lazberry.xmaslegacy.region.RegionManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class RegionCreateListener implements Listener {
	private final @NotNull RegionManager rm;

	@Inject
	public RegionCreateListener(@NotNull RegionManager rm) {
		this.rm = rm;
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
