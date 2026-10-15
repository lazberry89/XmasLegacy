package org.lazberry.xmaslegacy.party.commands;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.party.Party;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.FloodgateUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.Objects;
import java.util.UUID;

@Slf4j
public class PartyCommandInvite implements SubCommand {
    private final PartyManager pm;

    public PartyCommandInvite(PartyManager pm) {
		this.pm = pm;
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var uuid = player.getUniqueId();

        if (args.length >= 2) {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                InfoUtils.error(player, "플레이어를 찾을 수 없습니다.");
                return;
            }
            var target = targetPlayer.getUniqueId();

            var party = pm.getParty(target);
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
            if (FloodgateUtils.isFloodgate(target)) {
                targetPlayer.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    var floodgatePlayer = FloodgateApi.getInstance().getPlayer(target);
                    if (floodgatePlayer != null) {
                        floodgatePlayer.sendForm(inviteComp(current, targetPlayer));
                    } else log.error("Failed to find mobile player: {}", targetPlayer.getName());
                }, 15L);
            }
            else Bukkit.getScheduler().runTaskLater(plugin, () -> targetPlayer.sendMessage(inviteComp(current)), 2L);
            InfoUtils.info(player, "파티 초대 요청을 보냈습니다.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }

	private String getNameByUUID(UUID uuid) {
		var player = Bukkit.getOfflinePlayer(uuid);
		return player.hasPlayedBefore() ? player.getName() : "&k???";
	}

    private @NotNull Component inviteComp(@NotNull Party party) {
        var options = ClickCallback.Options.builder()
                .uses(1)
                .lifetime(java.time.Duration.ofMinutes(3))
                .build();
        Component accept = ColorUtils.chat("&a&l[수락]")
		        .hoverEvent(HoverEvent.showText(
						ColorUtils.chat("클릭하여 파티에 참가하세요. -> &6" + getNameByUUID(party.getLeader()) + "님의 파티")))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p && p.isOnline()) {
                        var uuid  = p.getUniqueId();
                        if (pm.joinParty(party.getLeader(), uuid)) {
                            InfoUtils.info(p, "파티에 참가했습니다.");
                            party.getMembers().stream().map(Bukkit::getPlayer)
                                    .filter(Objects::nonNull)
                                    .filter(Player::isOnline)
                                    .filter(Player::isValid)
                                    .forEach(t -> InfoUtils.info(t, "&6" + p.getName() + "&f님이 파티에 참가했습니다."));
                        }
                        else InfoUtils.error(p, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
                    }
                }, options));
        return ColorUtils.chat(Alert.XmasLegacy + " 파티참가 요청이 왔습니다. (초대자 : &6" + getNameByUUID(party.getLeader()) + "&f) ").append(accept);
    }

    private @NotNull SimpleForm inviteComp(@NotNull Party party, @NotNull Player player) {
        UUID uuid = player.getUniqueId();
        return SimpleForm.builder()
                .title("§6§l파티 참가 제안")
                .content("파티초대가 왔습니다.\n" + "초대자 : §6" + getNameByUUID(party.getLeader()) + "\n" + "§r파티원 수 : §6" + party.getMembers().size() + " / 4")
                .button("§a§l[ 수락 ]")
                .button("§c§l[ 거절 ]")

                .validResultHandler(response -> {
                    int buttonId = response.clickedButtonId();

                    if (buttonId == 0) {

                        if (pm.joinParty(party.getLeader(), uuid)) {
                            InfoUtils.info(player, "파티에 참가했습니다.");
                            party.getMembers().stream()
                                    .map(Bukkit::getPlayer)
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
