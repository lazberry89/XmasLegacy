package org.lazberry.xmaslegacy.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This class handles Server to Server transfer. Use plugin messenger at "bungeecord:main", sends
 * packet-byte message to Velocity. Also handles Party logics like moving another members if target player
 * is party leader. Also have option to hide where to go, force or not player to move.
 * @see org.bukkit.plugin.messaging.Messenger
 * @see org.lazberry.xmaslegacy.party.Party
 * @see org.bukkit.Server
 * @see org.lazberry.xmaslegacy.user.User
 */
@Slf4j
@UtilityClass
public final class ServerTransfer {
	private static final Set<UUID> transferring = new HashSet<>();
	private static @Setter UserManager um;
	private static @Setter PartyManager pm;

    /**
     * Lazy-Initialize of plugin instance. Due to static class, there would be possibility that
     * plugin instance is null. So only get instance when methods are used.
     * @return plugin instance
     */
    @Contract(value = "-> !null", pure = true)
	private @NotNull XmasLegacy plugin() {
		return XmasLegacy.getInstance();
	}

    /**
     * Reusing method {@link ServerTransfer#dramaticTeleport(Player, Location, long)}, but duration is set to 40(Long)
     * as basic value.
     * @param player target player to move
     * @param to where to move
     */
	public void dramaticTeleport(@NotNull Player player, @NotNull Location to) {
		dramaticTeleport(player, to, 40L);
	}

	public void dramaticTeleport(@NotNull Player player, @NotNull Location to, Runnable runnable) {
		dramaticTeleport(player, to, 40L, runnable);
	}

    /**
     * Transports target player to {@link Location} that is previously set. Giving {@link Sound}, visual effect
     * to player and applying delay to make teleport most dramatic. Also, {@link org.lazberry.xmaslegacy.user.User} instance must be loaded if player want to
     * teleport. if not loaded, sends reload alert.
     * <pre>{@code
     * Player p = e.getPlayer();
     * ServerTransfer.dramaticTeleport(p, targetLoc, 60L);
     * }</pre>
     * @param player target player to move
     * @param to where to move
     * @param duration how long the effect lasts.
     * @see ServerTransfer#dramaticTeleport(Player, Location)
     * @see UserHandler#sendReloadNotice(Player)
     * @see Location
     */
	public void dramaticTeleport(@NotNull Player player, @NotNull Location to, long duration, Runnable runnable) {
		var uuid = player.getUniqueId();
		var user = um.getUser(uuid);
		if (user == null) {
			UserHandler.sendReloadNotice(player);
			log.error("Failed to move {} to target Location.(User info not loaded)", player.getName());
			return;
		}

		if (transferring.contains(uuid)) return;
		transferring.add(uuid);

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
		}, duration / 2 );
		Bukkit.getScheduler().runTaskLater(plugin(), () -> {
			StunUtils.release(uuid);
			transferring.remove(uuid);
			runnable.run();
		}, duration);
	}

	public void dramaticTeleport(@NotNull Player player, @NotNull Location to, long duration) {
		dramaticTeleport(player, to, duration, () -> {});
	}

    public boolean transfer(@NotNull ServerType toServer, @NotNull Player... players) {
        return Arrays.stream(players).allMatch(p -> sendBungeePacket(toServer, p));
    }

    public boolean transfer(@NotNull ServerType toServer, @NotNull Player player, boolean force, boolean hide) {
        if (!force) {
            player.sendMessage(askComponent(toServer, hide, player, FloodgateUtils.isFloodgate(player)));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            return true;
        }
        return transfer(toServer, player);
    }

    /**
     * Main transport logic. Transfers target player to intended server. The Channel on
     * {@link org.bukkit.plugin.messaging.Messenger#registerOutgoingPluginChannel(Plugin, String)} at "bungeecord:main" should be
     * opened in {@link org.lazberry.xmaslegacy.PluginUtils.Initializer.GlobalInitializer}. Also, when player is in party, two cases.
     * First, as general member, leaves {@link org.lazberry.xmaslegacy.party.Party} and transfers server. Second, as Leader of a party, brings all party member with him/her.
     * But fails when user info is not loaded, not sending any notice.(Should handle this case.)
     * <pre>{@code
     * if (ServerTransfer.transfer(ServerType.HUNTING, p)) {
     *     p.sendMessage("Transfer successful.");
     * } else {
     *     p.sendMessage("Transfer failed!");
     * }
     * }</pre>
     * @param toServer target server as destination
     * @param player target player to move
     * @return false when fails, true.
     * @see org.lazberry.xmaslegacy.party.Party
     * @see org.lazberry.xmaslegacy.user.User
     * @see ServerTransfer#transfer(ServerType, Player, boolean, boolean)
     * @see ServerTransfer#transfer(ServerType, Player...)
     */
    @CheckReturnValue
    public boolean transfer(@NotNull ServerType toServer, @NotNull Player player) {
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

    /**
     * Actual logic that sends packet to move player. On opened
     * {@link org.bukkit.plugin.messaging.Messenger#registerOutgoingPluginChannel(Plugin, String)} channel, this method
     * sends plugin message to Velocity plugin to transfer player.
     * @param toServer target server to move as destination
     * @param player target player to move
     * @return false when fails, true
     * @see org.bukkit.plugin.messaging.Messenger#registerOutgoingPluginChannel(Plugin, String)
     * @see Player#sendPluginMessage(Plugin, String, byte[])
     */
    private boolean sendBungeePacket(@NotNull ServerType toServer, @NotNull Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(toServer.name());

        try {
            player.sendPluginMessage(plugin(), "bungeecord:main", out.toByteArray());
            return true;
        } catch (IllegalArgumentException e) {
            plugin().getSLF4JLogger().error("Error occurred while transferring player {}", player, e);
            return false;
        }
    }

    /**
     * Sends asking message to player that are in party. This method works when party leader moves server, and party members can choose
     * to follow or not if valid.(There are option to force or not.) Also, it handles mobile, java two cases. When mobile(Floodgate),
     * it sends Form using button. When Java, sends click-able Component message.
     * @param type where to transfer player
     * @param hide to hide or not where to transfer.
     * @param p target player to move
     * @param isFloodgate if player is from floodgate or not
     * @return componented message.
     * @see ServerType
     * @see Component
     */
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
			} else {
				InfoUtils.error(p, "모바일 유저 객체를 가져오지 못했습니다. 관리자에게 문의해주세요.");
				log.error("Failed to load mobile player instance. Player: {}", p.getName());
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