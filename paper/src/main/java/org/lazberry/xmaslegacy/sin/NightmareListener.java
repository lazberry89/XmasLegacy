package org.lazberry.xmaslegacy.sin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class NightmareListener implements Listener {
	private final NightmareManager nightmareManager;

	@Inject
	public NightmareListener(NightmareManager nightmareManager) {
		this.nightmareManager = nightmareManager;
	}

	@EventHandler
	public void resetWhenLeave(PlayerQuitEvent e) {
		var player = e.getPlayer();
		if (nightmareManager.hasBackup(player)) {
			nightmareManager.cleanup(player);
		}
	}
}
