package org.lazberry.xmaslegacy.PartyCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.Party;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.ServerTransfer;
import org.lazberry.xmaslegacy.Utils.SubCommand;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.Objects;
import java.util.UUID;

public class PartyCommandInvite implements SubCommand {
    private final @NotNull UserManager um;
    private final @NotNull PartyManager pm;

    public PartyCommandInvite() {
        this.um = UserManager.INSTANCE;
        this.pm = PartyManager.INSTANCE;
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var uuid = player.getUniqueId();
        var user = um.getUser(uuid);
        if (user == null) {
            ServerTransfer.sendReloadNotice(player);
            return;
        }
        if (args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                InfoUtils.error(player, "플레이어를 찾을 수 없습니다.");
                return;
            }
            User targetUser = um.getUser(target.getUniqueId());
            if (targetUser == null) {
                InfoUtils.error(player, "해당 유저의 정보가 로드되지 않았습니다.");
                return;
            }
            var party = pm.getParty(target.getUniqueId());
            var current = pm.getParty(uuid);
            if (current == null) {
                InfoUtils.error(player, "소속되어있는 파티가 없습니다. 먼저 파티를 생성하세요.");
                return;
            }
            if (party != null) {
                InfoUtils.error(player, "해당 플레이어는 이미 다른 파티에 소속되어 있습니다.");
                return;
            }
            if (current.isFull()) {
                InfoUtils.error(player, "파티가 가득 찼습니다.");
                return;
            }
            var plugin = XmasLegacy.getInstance();
            if (targetUser.isMobile())
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    var floodgatePlayer = FloodgateApi.getInstance().getPlayer(target.getUniqueId());
                    if (floodgatePlayer != null) {
                        floodgatePlayer.sendForm(inviteComp(current, target));
                    }
                }, 5L);
            else Bukkit.getScheduler().runTaskLater(plugin, () -> target.sendMessage(inviteComp(current)), 2L);
            InfoUtils.info(player, "파티 초대 요청을 보냈습니다.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }

    private @NotNull Component inviteComp(@NotNull Party party) {
        var options = ClickCallback.Options.builder()
                .uses(1)
                .lifetime(java.time.Duration.ofMinutes(3))
                .build();
        Component accept = ColorUtils.chat("&a&l[수락]").hoverEvent(HoverEvent.showText(ColorUtils.chat("클릭하여 파티에 참가하세요. -> &6" + party.getLeader().getName() + "님의 파티")))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p && p.isOnline()) {
                        var user = um.getUser(p.getUniqueId());
                        if (user == null) {
                            ServerTransfer.loadUser(p, false);
                            return;
                        }
                        if (pm.joinParty(party.getLeader(), user)) {
                            InfoUtils.info(p, "파티에 참가했습니다.");
                            party.getMembers().stream().map(m -> Bukkit.getPlayer(m.getUniqueId()))
                                    .filter(Objects::nonNull)
                                    .filter(Player::isOnline)
                                    .filter(Player::isValid)
                                    .forEach(t -> InfoUtils.info(t, "&6" + p.getName() + "&f님이 파티에 참가했습니다."));
                        }
                        else InfoUtils.error(p, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
                    }
                }, options));
        return ColorUtils.chat(Alert.XmasLegacy + " 파티참가 요청이 왔습니다. (초대자 : &6" + party.getLeader().getName() + "&f) ").append(accept);
    }

    private @NotNull SimpleForm inviteComp(@NotNull Party party, @NotNull Player player) {
        UUID uuid = player.getUniqueId();
        return SimpleForm.builder()
                .title("§6§l파티 참가 제안")
                .content("파티초대가 왔습니다.\n" + "초대자 : §6" + party.getLeader().getName() + "\n" + "§r파티원 수 : §6" + party.getMembers().size() + " / 4")
                .button("§a§l[ 수락 ]")
                .button("§c§l[ 거절 ]")

                .validResultHandler(response -> {
                    int buttonId = response.clickedButtonId();

                    if (buttonId == 0) {
                        var user = um.getUser(uuid);
                        if (user == null) {
                            ServerTransfer.loadUser(player, false);
                            return;
                        }

                        if (pm.joinParty(party.getLeader(), user)) {
                            InfoUtils.info(player, "파티에 참가했습니다.");
                            party.getMembers().stream()
                                    .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                                    .filter(Objects::nonNull)
                                    .filter(Player::isOnline)
                                    .filter(Player::isValid)
                                    .forEach(t -> InfoUtils.info(t, "&6" + player.getName() + "&f님이 파티에 참가했습니다."));
                        }
                        else InfoUtils.error(player, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
                    } else InfoUtils.warn(player, "파티 참가 제안이 거절되었습니다.");
                })
                .closedResultHandler(() -> InfoUtils.warn(player, "파티 참가 제안이 취소되었습니다."))
                .build();
    }
}
