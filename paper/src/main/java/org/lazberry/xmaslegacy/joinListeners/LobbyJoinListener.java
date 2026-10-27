package org.lazberry.xmaslegacy.joinListeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.location.DestinationType;
import org.lazberry.xmaslegacy.location.Lobby.LobbyManager;
import org.lazberry.xmaslegacy.location.SpawnRepository;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;

@Listeners
@Registry.Include(type = ServerType.LOBBY)
public class LobbyJoinListener implements Listener {
	private final @NotNull SpawnRepository spawnRepo;

	@Inject
	public LobbyJoinListener(@NotNull SpawnRepository spawnRepo) {
		this.spawnRepo = spawnRepo;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.joinMessage(null);
		e.getPlayer().sendMessage(ColorUtils.chat(Alert.XmasLegacy + " 입장을 환영합니다! 전방의 포탈로 게임을 시작하세요."));
		LobbyManager lbm = spawnRepo.get(DestinationType.LOBBY);
		lbm.lobbyJoin(e);
	}
}
