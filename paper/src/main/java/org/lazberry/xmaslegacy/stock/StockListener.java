package org.lazberry.xmaslegacy.stock;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class StockListener implements Listener {
	private final StockManager sm;
	private final XmasLegacy plugin;

	@Inject
	public StockListener(StockManager sm, XmasLegacy plugin) {
		this.sm = sm;
		this.plugin = plugin;
	}
}
