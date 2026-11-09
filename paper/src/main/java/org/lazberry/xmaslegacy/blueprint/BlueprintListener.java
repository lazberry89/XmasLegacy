package org.lazberry.xmaslegacy.blueprint;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;

@Listeners
@Components.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintListener implements Listener {
	private final BluePrintManager blueprintManager;

	@Inject
	public BlueprintListener(BluePrintManager blueprintManager) {
		this.blueprintManager = blueprintManager;
	}

	@EventHandler
	public void whenClickBlueprint(PlayerInteractEvent e) {
		var p = e.getPlayer();
		if (e.getAction().isLeftClick()) return;

		var item = e.getItem();
		if (item == null || item.getType().isAir()) return;
		if (!BluePrint.isBluePrint(item)) return;

		var name = BluePrint.getNameByItem(item);
		if (name == null) return;

		blueprintManager.getBluePrint(name).ifPresent(blueprint -> {
			if (blueprintManager.startBuilding(blueprint.getStructureName(), p, () -> {})) item.setAmount(0);
		});
	}
}
