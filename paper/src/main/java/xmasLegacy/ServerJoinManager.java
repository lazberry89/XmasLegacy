package xmasLegacy;

import net.kyori.adventure.text.Component;
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

@SuppressWarnings("ClassCanBeRecord")
public class ServerJoinManager implements Listener {
	private final UserManager UM;
	private final XmasLegacy plugin;

	public ServerJoinManager(UserManager UM, XmasLegacy plugin) {
		this.UM = UM;
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void JoinMsg(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		e.joinMessage(null);
		boolean isFloodgate = Bukkit.getPluginManager().isPluginEnabled("floodgate")
				&& FloodgateApi.getInstance().isFloodgatePlayer(p.getUniqueId());

		switch (plugin.getServerType().toLowerCase()) {
			case "main" ->
					UM.onJoinAsync(p.getUniqueId(), p.getName(), true).whenComplete((user, throwable) -> {
						if (throwable != null) {
							Bukkit.getScheduler().runTask(plugin, () -> {
								Component reload = ColorUtils.chat(" &c&l[ 다시 로드하기 ]").clickEvent(ClickEvent.runCommand("/0947345")).hoverEvent(HoverEvent.showText(ColorUtils.chat("&c&l클릭하여 유저 정보를 다시 로드합니다.")));
								p.sendMessage(ColorUtils.chat(Alert.RED + " 유저 정보 로드 중 시스템 내부 예외가 발생했습니다!").append(reload));
								plugin.getSLF4JLogger().error("비동기 유저 로드 중 치명적 예외 발생 (UUID: {})", p.getUniqueId(), throwable);
							});
							return;
						}

						Bukkit.getScheduler().runTask(plugin, () -> {
							if (!p.isOnline()) return;

							if (user == null) {
								p.sendMessage(ColorUtils.chat(Alert.RED + " 당신의 유저정보 로드가 실패했어요. 관리자를 호출해 주세요!"));
								plugin.getSLF4JLogger().error("서버 접속 중 유저 정보 로드 실패 (null 반환): {}, {}", p.getName(), p.getUniqueId());
								return;
							}

							if (user.isNewUser()) {
								Bukkit.broadcast(ColorUtils.chat(String.format(Alert.XmasLegacy + "&6&l %s&f 님의 첫 접속입니다. 환영해주세요!\uD83C\uDF84", p.getName())));
								p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
								p.spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(0, 1, 0), 5, 0.5, 1.2, 0.5, 0.01);

								if (isFloodgate) {
									user.addDollars(Constants.BASIC_MONEY_MOBILE);
									p.sendMessage(ColorUtils.chat(Alert.GREEN + " 모바일 접속 보너스가 지급되었습니다."));
								} else {
									user.addDollars(Constants.BASIC_MONEY_NORMAL);
								}
							} else {
								Bukkit.broadcast(ColorUtils.chat(String.format(Alert.XmasLegacy + "&6&l %s&f 님이 접속했어요!", p.getName())));
							}
							user.setNewUser(false);
						});
					});
			case "lobby" -> e.joinMessage(ColorUtils.chat(Alert.XmasLegacy + " 입장을 환영합니다! 전방의 포탈로 게임을 시작하세요."));
			default -> plugin.getSLF4JLogger().warn("알 수 없는 서버 타입입니다: {}", plugin.getServerType());
		}
	}

	@EventHandler
	public void LeaveMsg(PlayerQuitEvent e) {
		e.quitMessage(null);
		UM.onQuitAsync(e.getPlayer().getUniqueId());
	}
}