package org.lazberry.xmaslegacy.blueprint.selection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintSelectionListener implements Listener {
	private final BlueprintSelectionManager selectionManager;

	@Inject
	public BlueprintSelectionListener(BlueprintSelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	@EventHandler
	public void whenPlayerSelectPose(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		var uuid = player.getUniqueId();
		var action = e.getAction();

		var item = e.getItem();
		if (item == null || !selectionManager.isTool(item)) return;

		var block = e.getClickedBlock();
		if (block == null) return;
		var loc = block.getLocation();

		if (action == Action.LEFT_CLICK_BLOCK) {
			e.setCancelled(true);

			selectionManager.addFirstSelection(uuid, loc);
			InfoUtils.info(player, "첫번째 위치를 선정했습니다.");
			return;
		}
		if (action == Action.RIGHT_CLICK_BLOCK) {
			e.setCancelled(true);

			selectionManager.addSecondSelection(uuid, loc);
			InfoUtils.info(player, "두번째 위치를 선정했습니다");
		}
	}
}
