package xmasLegacy;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
//import org.geysermc.floodgate.api.FloodgateApi;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Prefix;
import org.lazberry.xmaslegacy.User.UserManager;

@SuppressWarnings("ClassCanBeRecord")
public class ServerJoinManager implements Listener {
	private final UserManager UM;
	private final XmasLegacy plugin; // BukkitScheduler 사용을 위해 필요
//	private final FloodgateApi gate = FloodgateApi.getInstance();

	public ServerJoinManager(UserManager UM, XmasLegacy plugin) {
		this.UM = UM;
		this.plugin = plugin;
	}

	@EventHandler
	public void JoinMsg(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		// 기본 접속 메시지는 일단 안 보이게 설정 (로드 완료 후 직접 띄우기 위함)
		e.joinMessage(null);


		// 비동기 로드 시작
		UM.onJoinAsync(p.getUniqueId(), p.getName(), true).thenAccept(user -> {
			// [Async Thread] 로드 완료 시점

			// 다시 메인 스레드(Sync)로 돌아와서 마인크래프트 효과 실행
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (!p.isOnline()) return; // 로드되는 동안 나갔을 경우 방지

				if (user.isNewUser()) {
					// 첫 접속 효과
					Bukkit.broadcast(ColorUtils.chat(String.format(Prefix.XmasLegacy + "&6&l %s&f 님의 첫 접속입니다. 환영해주세요!\uD83C\uDF84", p.getName())));
					p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
					p.spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(0, 1, 0), 5, 0.5, 1.2, 0.5, 0.01);

//					if (isFloodgate) {
//						p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 모바일 접속 보너스 5,000$가 지급되었습니다."));
//					}
				} else {
					// 일반 접속 메시지
					Bukkit.broadcast(ColorUtils.chat(String.format(Prefix.XmasLegacy + "&6&l %s&f 님이 접속했어요!", p.getName())));
				}
			});
		});
	}

	@EventHandler
	public void LeaveMsg(PlayerQuitEvent e) {
		e.quitMessage(null);
		UM.onQuitAsync(e.getPlayer().getUniqueId());
	}
}