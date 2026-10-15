package org.lazberry.xmaslegacy.party.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public class PartyCommandExpel implements SubCommand {
	private final PartyManager pm;

	public PartyCommandExpel(PartyManager pm) {
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
            var current = pm.getParty(player.getUniqueId());

            if (current == null) {
                InfoUtils.error(player, "소속되어 있는 파티가 없습니다.");
                return;
            }
            if (party == null) {
                InfoUtils.error(player, "해당 플레이어가 소속된 파티를 찾을 수 없습니다.");
                return;
            }
            if (!current.equals(party)) {
                InfoUtils.error(player, "서로 같은 파티가 아닙니다.");
                return;
            }
            if (!uuid.equals(current.getLeader())) {
                InfoUtils.error(player, "파티장만 유저를 추방시킬 수 있습니다.");
                return;
            }
            if (pm.leaveParty(target)) {
                InfoUtils.warn(targetPlayer, "파티에서 추방당했습니다.");
                InfoUtils.info(player, "파티에서 추방했습니다.");
            } else InfoUtils.error(player, "파티에서 추방하지 못했습니다. 파티에 소속되어있는지 확인해주세요.");

        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
