package org.lazberry.xmaslegacy.collectors.hunter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class HunterBugListener implements Listener {
	private final HunterRepository repository;

	@Inject
	public HunterBugListener(HunterRepository repository) {
		this.repository = repository;
	}

	@EventHandler
	public void onPlayerHit(EntityDamageByEntityEvent e) {}
}
