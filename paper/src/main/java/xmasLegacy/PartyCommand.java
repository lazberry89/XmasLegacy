package xmasLegacy;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;

import java.util.ArrayList;
import java.util.List;

@Commands(command = "파티")
public class PartyCommand implements CommandExecutor, TabCompleter {
    private final @NotNull XmasLegacy plugin;
    private final @NotNull UserManager um;
    private final @NotNull PartyManager pm;

    public PartyCommand() {
        this.plugin = XmasLegacy.getInstance();
        this.um = UserManager.getInstance();
        this.pm = PartyManager.getInstance();
    }

    //파티 초대 <이름>
    //파티 참가 <이름>
    //파티 멤버 <이름>
    //파티 생성
    //파티 나가기
    //파티 참가
    //파티 멤버
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        var user = um.getUser(p.getUniqueId());
        if (user == null) {
            ServerTransfer.loadUser(p, false);
            return true;
        }
        if (args.length == 1) {
            switch (args[0]) {
                case "생성" -> {
                    if (pm.createParty(user)) {
                        p.sendMessage();
                    }
                }
                case "참가" -> {}
                case "멤버" -> {
                    var party = pm.getParty(p.getUniqueId());
                    if (party == null) {
                        plugin.infoMsg(InfoLevel.WARN, p, "소속되어있는 파티가 없습니다.");
                        return true;
                    }
                    List<User> members = party.getMembers();
                    List<String> notLeader = new ArrayList<>(members.stream()
                            .filter(u -> !u.equals(party.getLeader()))
                            .map(User::getName).toList());
                    if (members.isEmpty()) {
                        plugin.infoMsg(InfoLevel.ERROR, p, "파티원이 없네요..? 뭔가 이상해보입니다.");
                        return true;
                    }
                    Component msgLeader = ColorUtils.chat(Alert.YELLOW + " 파티 리더 : &6" + party.getLeader().getName());
                    Component msgMembers = ColorUtils.chat(Alert.YELLOW + " 파티 멤버 : &6" + (notLeader.isEmpty() ? "&c없음" : String.join(", ", notLeader)));
                    p.sendMessage(msgLeader.appendNewline().append(msgMembers));
                }
                case "나가기" -> {}
                default -> plugin.infoMsg(InfoLevel.ERROR, p, "유효한 명령어가 아닙니다.");
            }
        }
        if (args.length == 2) {

        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull ...args) {
        return List.of();
    }
}
