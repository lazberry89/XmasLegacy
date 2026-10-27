package org.lazberry.xmaslegacy.magic;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.lazberry.xmaslegacy.location.DestinationType;
import org.lazberry.xmaslegacy.location.SpawnRepository;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.utils.ServerTransfer;

@Registry.Include(type = ServerType.MAIN)
public class BookListener implements Listener {
	private final SpawnRepository repository;

	@Inject
	public BookListener(SpawnRepository repository) {
		this.repository = repository;
	}

	@EventHandler
	public void onBookClick(PlayerInteractEntityEvent e) {
		var player = e.getPlayer();
		var target = e.getRightClicked();

		if (KeyUtils.hasKey(target, MagicBook.key)) {
			Location destination = repository.get(DestinationType.PORT).getSpawn();
			if (destination == null) {
				InfoUtils.error(player, "위치가 설정되지 않았습니다. 관리자에게 문의해주세요.");
				return;
			}
			ServerTransfer.dramaticTeleport(player, destination);
		}
	}
}
