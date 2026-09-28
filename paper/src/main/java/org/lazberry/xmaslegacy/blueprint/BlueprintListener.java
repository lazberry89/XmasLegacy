package org.lazberry.xmaslegacy.blueprint;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintListener implements Listener {
	private final BluePrintManager blueprintManager;

	@Inject
	public BlueprintListener(BluePrintManager blueprintManager) {
		this.blueprintManager = blueprintManager;
	}

	@EventHandler
	public void whenClickBlueprint(PlayerInteractEvent e) {

	}
}
