package org.lazberry.xmaslegacy.role.selection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

@Listeners
@Components.Include(type = ServerType.MAIN)
public class RoleSelectionListener implements Listener {
	private final XmasLegacy plugin;

	@Inject
	public RoleSelectionListener(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onSelectionGuiClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;
	}
}
