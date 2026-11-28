package org.lazberry.xmaslegacy.utils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;

/**
 * Each window's own listener only sees clicks in the window itself. Shift-clicks from the player's inventory and
 * drags got past it: the items were gone, or in a shared shop window visible to everyone.
 */
@Listeners
@Components.Include(type = ServerType.GLOBAL)
public class ReadOnlyGuiListener implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void click(InventoryClickEvent e) {
		if (!(e.getView().getTopInventory().getHolder() instanceof ReadOnlyGui)) return;
		if (e.getClickedInventory() == e.getView().getTopInventory()
				|| e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
				|| e.getAction() == InventoryAction.COLLECT_TO_CURSOR) e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void drag(InventoryDragEvent e) {
		if (!(e.getView().getTopInventory().getHolder() instanceof ReadOnlyGui)) return;
		int topSize = e.getView().getTopInventory().getSize();
		if (e.getRawSlots().stream().anyMatch(slot -> slot < topSize)) e.setCancelled(true);
	}
}
