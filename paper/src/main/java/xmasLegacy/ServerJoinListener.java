package xmasLegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.geysermc.floodgate.api.FloodgateApi;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.ServerPrefix.UserTagManager;

public class ServerJoinListener implements Listener {
	private final UserManager UM;
	private final XmasLegacy plugin;

	public ServerJoinListener() {
		this.UM = UserManager.getInstance();
		this.plugin = XmasLegacy.getInstance();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void JoinMsg(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		e.joinMessage(null);

		switch (plugin.getServerType().toLowerCase()) {
			case "main" -> ServerTransfer.loadUser(p);
			case "lobby" -> e.joinMessage(ColorUtils.chat(Alert.XmasLegacy + " 입장을 환영합니다! 전방의 포탈로 게임을 시작하세요."));
			default -> plugin.getSLF4JLogger().warn("알 수 없는 서버 타입입니다: {}", plugin.getServerType());
		}
	}

	@EventHandler
	public void LeaveMsg(PlayerQuitEvent e) {
		e.quitMessage(null);
		Player p = e.getPlayer();
		UserTagManager.removeHoverTag(p);
		UM.onQuitAsync(p.getUniqueId());
	}
}