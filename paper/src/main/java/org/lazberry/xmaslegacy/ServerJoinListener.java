package org.lazberry.xmaslegacy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.ServerInitializer;
import org.lazberry.xmaslegacy.SavingLocation.DestinationType;
import org.lazberry.xmaslegacy.SavingLocation.Lobby.LobbyManager;
import org.lazberry.xmaslegacy.SavingLocation.MainSpawnManager;
import org.lazberry.xmaslegacy.SavingLocation.SpawnRepository;
import org.lazberry.xmaslegacy.PlayerUtils.UserTagManager;
import org.lazberry.xmaslegacy.Utils.ServerTransfer;

public final class ServerJoinListener implements Listener {
	private final @NotNull UserManager um;
	private final @NotNull XmasLegacy plugin;

	public ServerJoinListener() {
		this.um = UserManager.INSTANCE;
		this.plugin = XmasLegacy.getInstance();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void JoinProcess(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		e.joinMessage(null);

		switch (ServerInitializer.getServerType(this.plugin)) {
			case MAIN -> {
				MainSpawnManager val = SpawnRepository.INSTANCE.get(DestinationType.MAIN);
				val.joinEffect(p);
				ServerTransfer.loadUser(p, true);
			}
			case LOBBY -> {
				e.joinMessage(ColorUtils.chat(Alert.XmasLegacy + " 입장을 환영합니다! 전방의 포탈로 게임을 시작하세요."));
				LobbyManager lbm = SpawnRepository.INSTANCE.get(DestinationType.LOBBY);
				lbm.lobbyJoin(e);
			}
			default -> {
				plugin.getSLF4JLogger().warn("알 수 없는 서버 타입입니다: {}", ServerInitializer.getServerType(this.plugin));
				p.kick(ColorUtils.chat("&c올바르지 않은 서버 타입입니다. config.yml을 수정하세요."), PlayerKickEvent.Cause.PLUGIN);
				Bukkit.getOnlinePlayers().forEach(Player::kick);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void LeaveMsg(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		UserTagManager.removeHoverTag(p);

		e.quitMessage(null);
		um.onQuitAsync(p.getUniqueId()).whenComplete((u, ex) -> {
			if (ex == null) plugin.getSLF4JLogger().info("User data saved for player: {}", p.getName());
			else plugin.getSLF4JLogger().error("Failed to save user data for player: {}", p.getName(), ex);
		});
	}
}