package xmasLegacy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.ServerPrefix.UserTagManager;

import java.util.Arrays;
import java.util.UUID;

public class ServerTransfer {
    private static final @NotNull XmasLegacy plugin = XmasLegacy.getInstance();

    public ServerTransfer() {}

    @CheckReturnValue
    public static boolean isFloodgate(@NotNull Player player) {
        return Bukkit.getPluginManager().isPluginEnabled("floodgate")
                && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }

	@CheckReturnValue
	public static boolean isFloodgate(@NotNull UUID uuid) {
		return Bukkit.getPluginManager().isPluginEnabled("floodgate")
				&& FloodgateApi.getInstance().isFloodgatePlayer(uuid);
	}

    public static void loadUser(@NotNull Player player) {
        @NotNull var um = UserManager.getInstance();
        um.onJoinAsync(player.getUniqueId(), player.getName(), true).whenComplete((user, throwable) -> {
            if (throwable != null || user == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;

                    var options = ClickCallback.Options.builder()
                            .uses(1)
                            .lifetime(java.time.Duration.ofMinutes(3))
                            .build();

                    Component reload = ColorUtils.chat(" &c&l[ 다시 로드하기 ]")
                            .hoverEvent(HoverEvent.showText(ColorUtils.chat("&c&l클릭하여 유저 정보를 다시 로드합니다.")))
                            .clickEvent(ClickEvent.callback(audience -> {
                                if (audience instanceof Player t) {
                                    um.onJoinAsync(t.getUniqueId(), t.getName(), true).whenComplete((reloadedUser, ex) -> Bukkit.getScheduler().runTask(plugin, () -> {
                                        if (ex != null || reloadedUser == null) {
                                            t.sendMessage(ColorUtils.chat(Alert.RED + " 다시 로드하는 데 실패했습니다. 관리자에게 문의하세요."));
                                        } else {
                                            plugin.infoMsg(InfoLevel.INFO, t, "유저정보가 성공적으로 로드되었습니다!");
                                            UserTagManager.createHoverTag(t, reloadedUser);
                                            UserTagManager.runTask();
                                        }
                                    }));
                                }
                            }, options));

                    player.sendMessage(ColorUtils.chat(Alert.RED + " 유저 정보 로드 중 시스템 내부 예외가 발생했습니다!").append(reload));
                    plugin.getSLF4JLogger().error("비동기 유저 로드 중 치명적 예외 발생 (UUID: {})", player.getUniqueId(), throwable);
                });
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
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

                user.setNewUser(false);
            });
        });
    }

    public static boolean transfer(@NotNull ServerType toServer, @NotNull Player... players) {
        return Arrays.stream(players).allMatch(p -> sendBungeePacket(toServer, p));
    }

    public static boolean transfer(@NotNull ServerType toServer, @NotNull Player player, boolean force, boolean hide) {
        if (!force) {
            player.sendMessage(askComponent(toServer, hide));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            return true;
        }
        return transfer(toServer, player);
    }

    @CheckReturnValue
    public static boolean transfer(@NotNull ServerType toServer, @NotNull Player player) {
        @NotNull var pm = PartyManager.getInstance();
        @NotNull var um = UserManager.getInstance();
        UUID uuid = player.getUniqueId();

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

        pm.leaveParty(um.getUser(uuid));
        player.sendMessage(ColorUtils.chat(Alert.YELLOW + " 파티에서 탈퇴되어 단독으로 서버를 이동합니다."));

        return sendBungeePacket(toServer, player);
    }

    private static boolean sendBungeePacket(@NotNull ServerType toServer, @NotNull Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(toServer.str());

        try {
            player.sendPluginMessage(plugin, "bungeecord:main", out.toByteArray());
            return true;
        } catch (IllegalArgumentException e) {
            plugin.getSLF4JLogger().error("Error occurred while transferring player {}", player, e);
            return false;
        }
    }

    private static @NotNull Component askComponent(@NotNull ServerType type, boolean hide) {
        var options = ClickCallback.Options.builder()
                .uses(1)
                .lifetime(java.time.Duration.ofMinutes(3))
                .build();
        Component btn = ColorUtils.chat("&a&l[수락]").hoverEvent(HoverEvent.showText(ColorUtils.chat("클릭하여 이동하세요. -> &6" + (hide ? "&k???" : type.name()))))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p && p.isOnline()) {
						if (transfer(type, p)) {
							p.sendMessage(ColorUtils.chat(Alert.GREEN + " 서버 이동을 시작합니다..."));
						} else {
							p.sendMessage(ColorUtils.chat(Alert.RED + " 서버 이동 중 오류가 발생했습니다. 관리자에게 문의하세요."));
							plugin.getSLF4JLogger().error("Failed to transfer player {} to server {} via BungeeCord", p, type);
						}
                    }
                }, options));
        Component msg = ColorUtils.chat(Alert.XmasLegacy + " &6서버이동&f 제안이 왔습니다. 이동하시겠습니까? ");
        return msg.append(btn);
    }
}