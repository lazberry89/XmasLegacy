package org.lazberry.xmaslegacy.PartyCommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.ServerTransfer;
import org.lazberry.xmaslegacy.Utils.SubCommand;

public class PartyCommandJoin implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var pm = PartyManager.INSTANCE;
        var um = UserManager.INSTANCE;

        var user = um.getUser(player.getUniqueId());
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
            var current = pm.getParty(player.getUniqueId());
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
            if (pm.joinParty(party.getLeader(), user)) InfoUtils.info(player, "파티에 참가했습니다.");
            else InfoUtils.error(player, "파티에 참가하지 못했습니다. 파티가 가득 찼거나 이미 파티에 소속되어있을 수 있습니다.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
