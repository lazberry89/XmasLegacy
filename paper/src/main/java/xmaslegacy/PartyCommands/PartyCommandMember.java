package xmaslegacy.PartyCommands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.Party;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

import java.util.List;

public class PartyCommandMember implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var pm = PartyManager.INSTANCE;
        var um = UserManager.INSTANCE;
        if (args.length == 1) {
            var party = pm.getParty(player.getUniqueId());
            if (party == null) {
                InfoUtils.warn(player, "소속되어있는 파티가 없습니다.");
                return;
            }
            player.sendMessage(memberShowcase(party));
        } else if (args.length >= 2) {
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
            if (party == null) {
                InfoUtils.warn(player, "파티에 소속되어있지 않은 유저입니다.");
                return;
            }
            player.sendMessage(memberShowcase(party));

        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }

    private @NotNull Component memberShowcase(@NotNull Party party) {
        List<User> members = party.getMembers();
        List<String> notLeader = members.stream()
                .filter(u -> !u.equals(party.getLeader()))
                .map(User::getName).toList();
        if (members.isEmpty()) return ColorUtils.chat(Alert.XmasLegacy + " 파티원이 없네요..? 뭔가 이상해보입니다.");

        Component msgLeader = ColorUtils.chat(Alert.YELLOW + " 파티 리더 : &6" + party.getLeader().getName());
        Component msgMembers = ColorUtils.chat(Alert.YELLOW + " 파티 멤버 : &6" + (notLeader.isEmpty() ? "&c없음" : String.join(", ", notLeader)));
        return msgLeader.appendNewline().append(msgMembers);
    }
}
