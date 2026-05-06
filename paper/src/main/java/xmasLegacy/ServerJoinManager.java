package xmasLegacy;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.geysermc.floodgate.api.FloodgateApi;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.settings.Prefix;
import org.lazberry.xmaslegacy.User.UserManager;

@SuppressWarnings("ClassCanBeRecord")
public class ServerJoinManager implements Listener {
	private final UserManager UM;
	private final XmasLegacy plugin;

	public ServerJoinManager(UserManager UM, XmasLegacy plugin) {
		this.UM = UM;
		this.plugin = plugin;
	}

	@EventHandler
	public void JoinMsg(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		e.joinMessage(null);
		//FloodgateApi gate = FloodgateApi.getInstance();
		boolean isFloodgate = Bukkit.getPluginManager().isPluginEnabled("floodgate")
				&& FloodgateApi.getInstance().isFloodgatePlayer(p.getUniqueId());
		switch (plugin.getServerType()) {
			case "main" ->
			// 비동기 로드 시작
			UM.onJoinAsync(p.getUniqueId(), p.getName(), true).thenAccept(user ->
					Bukkit.getScheduler().runTask(plugin, () -> {
						if (!p.isOnline()) return;
						if (user.isNewUser()) {
							Bukkit.broadcast(ColorUtils.chat(String.format(Prefix.XmasLegacy + "&6&l %s&f 님의 첫 접속입니다. 환영해주세요!\uD83C\uDF84", p.getName())));
							p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
							p.spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(0, 1, 0), 5, 0.5, 1.2, 0.5, 0.01);

							if (isFloodgate) {
								user.addDollars(Constants.BASIC_MONEY_MOBILE);
								p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 모바일 접속 보너스가 지급되었습니다."));
							} else {
								user.addDollars(Constants.BASIC_MONEY_NORMAL);
							}
						} else {
							// 일반 접속 메시지
							Bukkit.broadcast(ColorUtils.chat(String.format(Prefix.XmasLegacy + "&6&l %s&f 님이 접속했어요!", p.getName())));
						}
						user.setNewUser(false);
					})
			);
			case "lobby" -> {
				e.joinMessage(ColorUtils.chat(Prefix.XmasLegacy + " 입장을 환영합니다! 전방의 포탈로 게임을 시작하세요."));
				p.playSound(p, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
			}
		}
	}

	@EventHandler
	public void LeaveMsg(PlayerQuitEvent e) {
		e.quitMessage(null);
		UM.onQuitAsync(e.getPlayer().getUniqueId());
	}
}