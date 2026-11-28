package org.lazberry.xmaslegacy.catalog;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.catalog.gui.CatalogInterface;
import org.lazberry.xmaslegacy.catalog.gui.CatalogSampleEditor;
import org.lazberry.xmaslegacy.catalog.gui.CatalogSelectInterface;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.joinListeners.PlayerDataReadyEvent;

@Listeners
@Components.Exclude(type = ServerType.LOBBY)
public class CatalogListener implements Listener {
	private final CatalogManager manager;

	@Inject
	public CatalogListener(CatalogManager manager) {
		this.manager = manager;
	}

	@EventHandler
	public void loadWhenDataReady(PlayerDataReadyEvent e) {
		manager.load(e.getPlayer());
	}

	@EventHandler
	public void unloadWhenQuit(PlayerQuitEvent e) {
		manager.unload(e.getPlayer().getUniqueId());
	}

	private static boolean isReadOnly(InventoryHolder holder) {
		return holder instanceof CatalogInterface || holder instanceof CatalogSelectInterface;
	}

	@EventHandler
	public void click(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;
		var top = e.getView().getTopInventory();
		if (!isReadOnly(top.getHolder())) return;

		e.setCancelled(true);
		if (e.getClickedInventory() == null) return;

		if (top.getHolder() instanceof CatalogSelectInterface) {
			CatalogType type = e.getClickedInventory() == top ? CatalogSelectInterface.typeAt(e.getSlot()) : null;
			if (type != null) manager.open(p, type);
		} else if (top.getHolder() instanceof CatalogInterface gui && e.getClickedInventory() != top && (e.isLeftClick() || e.isRightClick())) {
			manager.register(p, gui, e.getSlot(), e.isShiftClick());
		}
	}

	@EventHandler
	public void drag(InventoryDragEvent e) {
		if (isReadOnly(e.getView().getTopInventory().getHolder())) e.setCancelled(true);
	}

	@EventHandler
	public void saveSampleWhenClosed(InventoryCloseEvent e) {
		if (e.getInventory().getHolder() instanceof CatalogSampleEditor editor && e.getPlayer() instanceof Player p)
			manager.closeSampleEditor(p, editor);
	}
}
