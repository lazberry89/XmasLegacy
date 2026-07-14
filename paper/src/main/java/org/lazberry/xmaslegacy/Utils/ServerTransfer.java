package org.lazberry.xmaslegacy.Utils;

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
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.User.UserSaveManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.PluginUtils.ServerType;
import org.lazberry.xmaslegacy.ServerPrefix.PrefixManager;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@UtilityClass
public final class ServerTransfer {
	private @NotNull XmasLegacy plugin() {
		return XmasLegacy.getInstance();
	}

	public void dramaticTeleport(@NotNull Player player, @NotNull Location to) {
		dramaticTeleport(player, to, 40L);
	}

	public void dramaticTeleport(@NotNull Player player, @NotNull Location to, long duration) {
		var uuid = player.getUniqueId();
		var user = UserManager.INSTANCE.getUser(uuid);
		if (user == null) {
			sendReloadNotice(player);
			log.error("Failed to move {} to target Location.(User info not loaded)", player.getName());
			return;
		}

		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) duration, 2, true, false, false));
		StunUtils.stun(uuid, "이동");
		Bukkit.getScheduler().runTaskLater(plugin(), () -> {
			if (player.isOnline() && player.isValid()) {
				player.teleport(to);
				player.playSound(player, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.7f);
				player.playSound(player, Sound.ENTITY_WARDEN_HEARTBEAT, 0.4f, 1.0f);
				InfoUtils.warn(player, "&7지연 이동중..");
			} else {
				log.error("Failed to teleport {} to Port.", player.getName());
				InfoUtils.error(player, "서버 이동에 실패하였습니다. 재시도 해주세요.");
			}
		}, duration / 2);
		Bukkit.getScheduler().runTaskLater(plugin(), () -> StunUtils.release(uuid), duration);
	}

    public boolean transfer(@NotNull ServerType toServer, @NotNull Player... players) {
        return Arrays.stream(players).allMatch(p -> sendBungeePacket(toServer, p));
    }

    public boolean transfer(@NotNull ServerType toServer, @NotNull Player player, boolean force, boolean hide) {
        if (!force) {
            player.sendMessage(askComponent(toServer, hide, player, isFloodgate(player)));
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
                    .map(u -> Bukkit.getPlayer(u.getUniqueId()))
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
        out.writeUTF(toServer.configValue());

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
			var floodgatePlayer = FloodgateApi.getInstance().getPlayer(p.getUniqueId());

			if (floodgatePlayer != null) {
				Bukkit.getScheduler().runTask(plugin(), () -> {
					var form = org.geysermc.cumulus.form.SimpleForm.builder()
							.title("§6§l서버 이동 제안")
							.content("파티장으로부터 서버 이동 제안이 왔습니다.\n" +
									"§e" + (hide ? "???" : type.name()) + "§r 서버로 이동하시겠습니까?")
							.button("§a§l[ 수락 ]")
							.button("§c§l[ 거절 ]")
							.validResultHandler(response -> {
								if (response.clickedButtonId() == 0) {
									p.sendMessage(ColorUtils.chat("&a서버 이동을 시작합니다..."));
									if (transfer(type, p)) log.info("User {} Transfer to {}", p.getName(), type);
									else {
										log.error("Failed to transfer {} to {}", p.getName(), type);
										InfoUtils.error(p, "서버 이동에 실패하였습니다. 관리자를 호출해주세요.");
									}
								} else {
									p.sendMessage(ColorUtils.chat("&c서버 이동 제안을 거절했습니다."));
								}
							})
							.closedResultHandler(() ->
									p.sendMessage(ColorUtils.chat("&c서버 이동 제안이 취소되었습니다."))
							).build();

					floodgatePlayer.sendForm(form);
				});
			}
			return ColorUtils.chat(Alert.XmasLegacy + " &6서버이동&f 제안이 왔습니다. 화면의 팝업창을 확인해주세요!");
		}

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