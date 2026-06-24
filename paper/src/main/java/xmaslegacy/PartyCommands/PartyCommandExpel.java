package xmaslegacy.PartyCommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.ServerTransfer;
import xmaslegacy.Utils.SubCommand;

public class PartyCommandExpel implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var pm = PartyManager.INSTANCE;
        var uuid = player.getUniqueId();

        var user = UserManager.INSTANCE.getUser(uuid);
        if (user == null) {
            ServerTransfer.sendReloadNotice(player);
            return;
        }

        if (args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                InfoUtils.infoMsg(InfoLevel.ERROR, player, "플레이어를 찾을 수 없습니다.");
                return;
            }
            User targetUser = UserManager.INSTANCE.getUser(target.getUniqueId());
            if (targetUser == null) {
                InfoUtils.infoMsg(InfoLevel.ERROR, player, "해당 유저의 정보가 로드되지 않았습니다.");
                return;
            }
            var party = pm.getParty(target.getUniqueId());
            var current = pm.getParty(player.getUniqueId());

            if (current == null) {
                InfoUtils.infoMsg(InfoLevel.ERROR, player, "소속되어 있는 파티가 없습니다.");
                return;
            }
            if (party == null) {
                InfoUtils.infoMsg(InfoLevel.ERROR, player, "해당 플레이어가 소속된 파티를 찾을 수 없습니다.");
                return;
            }
            if (!current.equals(party)) {
                InfoUtils.infoMsg(InfoLevel.ERROR, player, "서로 같은 파티가 아닙니다.");
                return;
            }
            if (!user.equals(current.getLeader())) {
                InfoUtils.infoMsg(InfoLevel.ERROR, player, "파티장만 유저를 추방시킬 수 있습니다.");
                return;
            }
            if (pm.leaveParty(targetUser)) {
                InfoUtils.infoMsg(InfoLevel.WARN, target, "파티에서 추방당했습니다.");
                InfoUtils.infoMsg(InfoLevel.INFO, player, "파티에서 추방했습니다.");
            } else InfoUtils.infoMsg(InfoLevel.ERROR, player, "파티에서 추방하지 못했습니다. 파티에 소속되어있는지 확인해주세요.");

        } else InfoUtils.infoMsg(InfoLevel.ERROR, player, "유효하지 않은 명령어입니다.");
    }
}
