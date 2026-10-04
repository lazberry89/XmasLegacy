package org.lazberry.xmaslegacy.blueprint.preview;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintPreviewListener implements Listener {
	private final BlueprintPreviewManager previewManager;

	@Inject
	public BlueprintPreviewListener(BlueprintPreviewManager previewManager) {
		this.previewManager = previewManager;
	}

	@EventHandler
	public void showBlueprintPreview(PlayerToggleSneakEvent e) {
		if (!e.isSneaking()) return;

		Player player = e.getPlayer();
		var item = player.getInventory().getItemInMainHand();

		if (BluePrint.isBluePrint(item)) {
			if (player.getCooldown(item) > 0) {
				InfoUtils.error(player, "재사용 대기시간이 남았습니다.");
				return;
			}
			player.setCooldown(item, 20);
			e.setCancelled(true);
			previewManager.updatePreview(player, 30L);
		}
	}

	@EventHandler
	public void removePreviewWhenLeave(PlayerQuitEvent e) {
		previewManager.clearPreview(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void removePreviewWhenWorldChange(PlayerChangedWorldEvent e) {
		previewManager.clearPreview(e.getPlayer().getUniqueId());
	}
}
