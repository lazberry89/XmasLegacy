package org.lazberry.xmaslegacy.stock.container;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class StockContainerListener implements Listener {

	public StockContainerListener() {}

	@EventHandler
	public void onContainerPageTurn(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player player)) return;
		var inv = e.getClickedInventory();
		if (inv == null) return;

		if (inv.getHolder() instanceof StockContainer container) {
			int slot = e.getRawSlot();

			if (slot >= 45 && slot <= 53) {
				e.setCancelled(true);
				if (slot == 45) {
					container.prevPage();
					player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					player.updateInventory();
				} else
				if (slot == 53) {
					container.nextPage();
					player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					player.updateInventory();
				}
			} else e.setCancelled(false);
		}
	}
}
