package xmaslegacy.Region.Listeners;

import com.google.j2objc.annotations.UsedByReflection;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Annotation.Listeners;
import xmaslegacy.Region.Gui.RegionCreateInterface;
import xmaslegacy.Region.Region;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.XmasLegacy;

@Listeners
@UsedByReflection
public class RegionCreateListener implements Listener {
	private final @NotNull XmasLegacy plugin;
	private final @NotNull RegionManager rm;

	public RegionCreateListener() {
		this.plugin = XmasLegacy.getInstance();
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
				plugin.infoMsg(InfoLevel.INFO, p, "구역이 성공적으로 생성되었습니다!");
				p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
				p.closeInventory();
			} else {
				plugin.infoMsg(InfoLevel.ERROR, p, "구역을 생성할 수 없습니다! 이미 구역이 존재하거나 청크가 생성되지 않았습니다.");
				p.closeInventory();
			}
		}
	}
}
