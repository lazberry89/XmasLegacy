package org.lazberry.xmaslegacy.PartyCommands;

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
	private final @NotNull PartyManager pm;
	private final @NotNull UserManager um;

	public PartyCommandLeave(@NotNull UserManager um, @NotNull PartyManager pm) {
		this.pm = pm;
		this.um = um;
	}

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var user = um.getUser(player.getUniqueId());
        if (user == null) {
            UserHandler.sendReloadNotice(player);
            return;
        }
        if (args.length >= 1) {
            var party = pm.getParty(player.getUniqueId());
            if (party == null) return;

            List<Player> targets = party.getMembers().stream()
                    .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                    .filter(Objects::nonNull)
                    .filter(p -> !p.getUniqueId().equals(player.getUniqueId())) // 본인 제외
                    .filter(Player::isOnline)
                    .filter(Player::isValid)
                    .toList();
            if (pm.leaveParty(user)) {
                targets.forEach(t -> InfoUtils.info(t, "&6" + player.getName() + "&f님이 파티를 나갔습니다."));
                InfoUtils.info(player, "파티에서 나갔습니다.");
            } else InfoUtils.error(player, "파티에서 나가지 못했습니다. 파티에 소속되어있는지 확인해주세요.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
