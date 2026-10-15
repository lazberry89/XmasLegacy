package org.lazberry.xmaslegacy.party.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.utils.UserHandler;

import java.util.List;
import java.util.Objects;

public class PartyCommandLeave implements SubCommand {
	private final PartyManager pm;

	public PartyCommandLeave(PartyManager pm) {
		this.pm = pm;
	}

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var uuid = player.getUniqueId();
        if (args.length >= 1) {
            var party = pm.getParty(player.getUniqueId());
            if (party == null) return;

            List<Player> targets = party.getMembers().stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(p -> !p.getUniqueId().equals(player.getUniqueId())) // 본인 제외
                    .filter(Player::isOnline)
                    .filter(Player::isValid)
                    .toList();
            if (pm.leaveParty(uuid)) {
                targets.forEach(t -> InfoUtils.info(t, "&6" + player.getName() + "&f님이 파티를 나갔습니다."));
                InfoUtils.info(player, "파티에서 나갔습니다.");
            } else InfoUtils.error(player, "파티에서 나가지 못했습니다. 파티에 소속되어있는지 확인해주세요.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
