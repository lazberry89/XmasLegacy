package xmaslegacy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.PluginUtils.ServerInitializer;
import xmaslegacy.ServerPrefix.UserTagManager;
import xmaslegacy.Utils.ServerTransfer;

public class ServerJoinListener implements Listener {
	private final @NotNull UserManager um;
	private final @NotNull XmasLegacy plugin;

	public ServerJoinListener() {
		this.um = UserManager.INSTANCE;
		this.plugin = XmasLegacy.getInstance();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void JoinMsg(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		e.joinMessage(null);

		switch (ServerInitializer.getServerType()) {
			case MAIN -> ServerTransfer.loadUser(p, true);
			case LOBBY -> e.joinMessage(ColorUtils.chat(Alert.XmasLegacy + " 입장을 환영합니다! 전방의 포탈로 게임을 시작하세요."));
			default -> plugin.getSLF4JLogger().warn("알 수 없는 서버 타입입니다: {}", ServerInitializer.getServerType());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void LeaveMsg(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		UserTagManager.removeHoverTag(p);

		e.quitMessage(null);
		um.onQuitAsync(p.getUniqueId()).whenComplete((u, e1) -> {
			if (e1 == null) plugin.getSLF4JLogger().info("User data saved for player: {}", p.getName());
			else plugin.getSLF4JLogger().error("Failed to save user data for player: {}", p.getName(), e1);
		});
	}
}