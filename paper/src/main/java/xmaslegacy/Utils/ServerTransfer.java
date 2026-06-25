package xmaslegacy.Utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.ServerPrefix.UserTagManager;
import xmaslegacy.PluginUtils.ServerType;
import xmaslegacy.SkillEffectManager;
import xmaslegacy.XmasLegacy;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@UtilityClass
public final class ServerTransfer {
	private final @NotNull FloodgateApi instance = FloodgateApi.getInstance();

	private @NotNull XmasLegacy plugin() {
		return XmasLegacy.getInstance();
	}

	public void dramaticTeleport(@NotNull Player player, @NotNull Location to) {
		dramaticTeleport(player, to, 40L);
	}

	public void dramaticTeleport(@NotNull Player player, @NotNull Location to, long duration) {
		var uuid = player.getUniqueId();
		var sem = SkillEffectManager.INSTANCE;
		var user = UserManager.INSTANCE.getUser(uuid);
		if (user == null) {
			sendReloadNotice(player);
			log.error("Failed to move {} to port.(User info not loaded)", player.getName());
			return;
		}

		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) duration, 2, true, false, false));
		sem.StunEntity(uuid);
		Bukkit.getScheduler().runTaskLater(plugin(), () -> {
			if (player.isOnline() && player.isValid()) {
				player.teleport(to);
				player.playSound(player, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.7f);
				player.playSound(player, Sound.ENTITY_WARDEN_HEARTBEAT, 0.4f, 1.0f);
				InfoUtils.warn(player, "&7지연 이동중..");
			} else {
				log.error("Failed to teleport {} to Port.", player.getName());
			}
		}, duration / 2);
		Bukkit.getScheduler().runTaskLater(plugin(), () -> sem.deStun(uuid), duration);
	}

    @CheckReturnValue
    public boolean isFloodgate(@NotNull Player player) {
        return Bukkit.getPluginManager().isPluginEnabled("floodgate")
                && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }

	@CheckReturnValue
	public boolean isFloodgate(@NotNull UUID uuid) {
		return Bukkit.getPluginManager().isPluginEnabled("floodgate")
				&& FloodgateApi.getInstance().isFloodgatePlayer(uuid);
	}

    private @NotNull ClickCallback.Options option() {
        return ClickCallback.Options.builder()
                .uses(1)
                .lifetime(java.time.Duration.ofMinutes(3))
                .build();
    }

	public void sendReloadNotice(@NotNull Player player) {
		if (player.isOnline())
			player.sendMessage(ColorUtils.chat(Alert.RED + " 유저 정보가 로드되지 않았습니다.").append(reloadComponent()));
	}

	private @NotNull Component reloadComponent() {
		return ColorUtils.chat(" &c&l[ 다시 로드하기 ]")
				.hoverEvent(HoverEvent.showText(ColorUtils.chat("&c&l클릭하여 유저 정보를 다시 로드합니다.")))
				.clickEvent(ClickEvent.callback(audience -> {
					if (audience instanceof Player t && t.isOnline()) {
						loadUser(t, true);
					}
				}, option()));
	}

    private void sendMsg(@NotNull Player player, @NotNull User user) {
        Bukkit.getScheduler().runTask(plugin(), () -> {
            if (!player.isOnline()) return;

            if (user.isNewUser()) {
                Bukkit.broadcast(ColorUtils.chat(String.format(Alert.XmasLegacy + "&6&l %s&f 님의 첫 접속입니다. 환영해주세요!\uD83C\uDF84", player.getName())));
                player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                player.spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 5, 0.5, 1.2, 0.5, 0.01);

                if (isFloodgate(player)) {
                    user.addDollars(Constants.BASIC_MONEY_MOBILE);
                    player.sendMessage(ColorUtils.chat(Alert.GREEN + " 모바일 접속 보너스가 지급되었습니다."));
                } else {
                    user.addDollars(Constants.BASIC_MONEY_NORMAL);
                }
            } else {
                Bukkit.broadcast(ColorUtils.chat(String.format(Alert.XmasLegacy + "&6&l %s&f 님이 접속했어요!", player.getName())));
            }
            UserTagManager.createHoverTag(player, user);
            UserTagManager.updateHoverTag(player, user);
            user.setNewUser(false);
        });
    }

	private void sendError(@NotNull Player player, Throwable throwable) {
		Bukkit.getScheduler().runTask(plugin(), () -> {
			if (!player.isOnline()) return;
			player.sendMessage(ColorUtils.chat(Alert.RED + " 유저 정보 로드 중 시스템 내부 예외가 발생했습니다.").append(reloadComponent()));
			plugin().getSLF4JLogger().error("비동기 유저 로드 중 치명적 예외 발생 (UUID: {})", player.getUniqueId(), throwable);
		});
	}

	public void loadUser(@NotNull Player player, boolean msg) {
		@NotNull var um = UserManager.INSTANCE;
		um.onJoinAsync(player.getUniqueId(), player.getName(), true).whenComplete((user, throwable) -> {
			if (throwable != null || user == null) {
				if (msg) sendError(player, throwable);
				return;
			}
			if (msg) sendMsg(player, user);
		});
	}

    public boolean transfer(@NotNull ServerType toServer, @NotNull Player... players) {
        return Arrays.stream(players).allMatch(p -> sendBungeePacket(toServer, p));
    }

    public boolean transfer(@NotNull ServerType toServer, @NotNull Player player, boolean force, boolean hide) {
        if (!force) {
			boolean isFloodgate = instance.isFloodgatePlayer(player.getUniqueId());
            player.sendMessage(askComponent(toServer, hide, player, isFloodgate));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            return true;
        }
        return transfer(toServer, player);
    }

    @CheckReturnValue
    public boolean transfer(@NotNull ServerType toServer, @NotNull Player player) {
        @NotNull var pm = PartyManager.INSTANCE;
        @NotNull var um = UserManager.INSTANCE;
        UUID uuid = player.getUniqueId();
		var user = um.getUser(uuid);
		if (user == null) return false;

        if (!pm.isInParty(uuid)) return sendBungeePacket(toServer, player);

        if (pm.isLeader(uuid)) {
            var party = pm.getParty(uuid);
            if (party == null) return sendBungeePacket(toServer, player);

            party.getMembers().stream()
                    .map(u -> Bukkit.getPlayer(u.getUUID()))
                    .filter(p -> p != null && p.isOnline())
                    .forEach(p -> {
                        if (!p.equals(player)) p.sendMessage(ColorUtils.chat(Alert.GREEN + " 방장을 따라 서버를 이동합니다!"));
                        sendBungeePacket(toServer, p);
                    });
            return true;
        }

        pm.leaveParty(user);
        player.sendMessage(ColorUtils.chat(Alert.YELLOW + " 파티에서 탈퇴되어 단독으로 서버를 이동합니다."));

        return sendBungeePacket(toServer, player);
    }

    private boolean sendBungeePacket(@NotNull ServerType toServer, @NotNull Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(toServer.str());

        try {
            player.sendPluginMessage(plugin(), "bungeecord:main", out.toByteArray());
            return true;
        } catch (IllegalArgumentException e) {
            plugin().getSLF4JLogger().error("Error occurred while transferring player {}", player, e);
            return false;
        }
    }

	private @NotNull Component askComponent(@NotNull ServerType type, boolean hide, @NotNull Player p, boolean isFloodgate) {
		if (isFloodgate) {
			var floodgatePlayer = org.geysermc.floodgate.api.FloodgateApi.getInstance().getPlayer(p.getUniqueId());

			if (floodgatePlayer != null) {
				var form = org.geysermc.cumulus.form.SimpleForm.builder()
						.title("§6§l서버 이동 제안")
						.content("파티장으로부터 서버 이동 제안이 왔습니다.\n" +
								"§e" + (hide ? "???" : type.name()) + "§r 서버로 이동하시겠습니까?")
						.button("§a§l[ 수락 ]")
						.button("§c§l[ 거절 ]")
						.validResultHandler(response -> {
							if (response.clickedButtonId() == 0) {
								p.sendMessage(ColorUtils.chat("&a서버 이동을 시작합니다..."));
								transfer(type, p);
							} else {
								p.sendMessage(ColorUtils.chat("&c서버 이동 제안을 거절했습니다."));
							}
						})
						.closedResultHandler(() -> {
							p.sendMessage(ColorUtils.chat("&c서버 이동 제안이 취소되었습니다."));
						})
						.build();

				// 베드락 유저 화면에 팝업 딲!
				floodgatePlayer.sendForm(form);
			}

			// 폼을 띄웠으니 채팅창에는 가벼운 안내 멘트만 컴포넌트로 리턴
			return ColorUtils.chat(Alert.XmasLegacy + " &6서버이동&f 제안이 왔습니다. 화면의 팝업창을 확인해주세요!");
		}

		// 💻 2. 자바 에디션 유저용 기존 클릭 콜백 로직
		var options = ClickCallback.Options.builder()
				.uses(1)
				.lifetime(java.time.Duration.ofMinutes(3))
				.build();

		Component btn = ColorUtils.chat("&a&l[수락]")
				.hoverEvent(HoverEvent.showText(ColorUtils.chat("클릭하여 이동하세요. -> &6" + (hide ? "&k???" : type.name()))))
				.clickEvent(ClickEvent.callback(audience -> {
					if (audience instanceof Player target && target.isOnline()) {
						if (transfer(type, target)) {
							target.sendMessage(ColorUtils.chat(Alert.GREEN + " 서버 이동을 시작합니다..."));
						} else {
							target.sendMessage(ColorUtils.chat(Alert.RED + " 서버 이동 중 오류가 발생했습니다. 관리자에게 문의하세요."));
							plugin().getSLF4JLogger().error("Failed to transfer player {} to server {} via BungeeCord", target, type);
						}
					}
				}, options));

		Component msg = ColorUtils.chat(Alert.XmasLegacy + " &6서버이동&f 제안이 왔습니다. 이동하시겠습니까? ");
		return msg.append(btn);
	}
}