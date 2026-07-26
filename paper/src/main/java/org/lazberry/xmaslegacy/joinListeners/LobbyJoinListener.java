package org.lazberry.xmaslegacy.joinListeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.SavingLocation.DestinationType;
import org.lazberry.xmaslegacy.SavingLocation.Lobby.LobbyManager;
import org.lazberry.xmaslegacy.SavingLocation.SpawnRepository;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

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
