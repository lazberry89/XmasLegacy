package org.lazberry.xmaslegacy.shop.showcase;

import org.bukkit.Particle;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class ShowCaseListener implements Listener {
	private final ShowCaseManager showCaseManager;

	@Inject
	public ShowCaseListener(ShowCaseManager showCaseManager) {
		this.showCaseManager = showCaseManager;
	}

	@EventHandler
	public void whenClickInteractionOfPurchase(PlayerInteractEntityEvent event) {
		var player = event.getPlayer();
		var loc = player.getLocation();
		if (event.getRightClicked() instanceof Interaction interaction) {
			String value = KeyUtils.get(interaction, ShowCase.key, PersistentDataType.STRING);
			if (value == null) return;
			if (player.isSneaking()) {
				if (showCaseManager.purchase(player, value)) {
					InfoUtils.info(player, "구매하였습니다!");
					InfoUtils.warn(player, "아이템이 지급되지 않았을 시 가방을 확인해보세요.");
					player.spawnParticle(Particle.HAPPY_VILLAGER, loc, 7, 0.3, 0.3, 0.3, 0.01);
				} else {
					InfoUtils.error(player, "구매에 실패하였습니다. 잔액이 부족하거나 상품이 존재하지 않습니다.");
					player.spawnParticle(Particle.ANGRY_VILLAGER, loc, 3, 0.3, 0.3, 0.3, 0.01);
				}
			} else {
				showCaseManager.sendInfo(player, value);
			}
		}
	}
}
