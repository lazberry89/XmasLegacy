package org.lazberry.xmaslegacy.party.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public class PartyCommandJoin implements SubCommand {
	private final PartyManager pm;

	public PartyCommandJoin(PartyManager pm) {
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
            if (current != null) {
                InfoUtils.error(player, "이미 파티에 소속되어 있습니다. 파티에 참가하려면 먼저 현재 파티에서 나가야 합니다.");
                return;
            }
            if (party == null) {
                InfoUtils.error(player, "해당 플레이어가 소속된 파티를 찾을 수 없습니다.");
                return;
            }
            if (party.isFull()) {
                InfoUtils.error(player, "파티가 가득 찼습니다.");
                return;
            }
            if (pm.joinParty(party.getLeader(), uuid)) InfoUtils.info(player, "파티에 참가했습니다.");
            else InfoUtils.error(player, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
