package org.lazberry.xmaslegacy.party.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.party.Party;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.List;
import java.util.UUID;

public class PartyCommandMember implements SubCommand {
	private final PartyManager pm;

	public PartyCommandMember(PartyManager pm) {
		this.pm = pm;
	}

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length == 1) {
            var party = pm.getParty(player.getUniqueId());
            if (party == null) {
                InfoUtils.warn(player, "소속되어있는 파티가 없습니다.");
                return;
            }
            player.sendMessage(memberShowcase(party));
        } else if (args.length >= 2) {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                InfoUtils.error(player, "플레이어를 찾을 수 없습니다.");
                return;
            }
            var target = targetPlayer.getUniqueId();
            var party = pm.getParty(target);
            if (party == null) {
                InfoUtils.warn(player, "파티에 소속되어있지 않은 유저입니다.");
                return;
            }
            player.sendMessage(memberShowcase(party));

        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }

	private String getNameByUUID(UUID uuid) {
		var player = Bukkit.getOfflinePlayer(uuid);
		return player.hasPlayedBefore() ? player.getName() : "&k???";
	}

    private @NotNull Component memberShowcase(@NotNull Party party) {
        List<UUID> members = party.getMembers();
        List<String> notLeader = members.stream()
                .filter(u -> !u.equals(party.getLeader()))
                .map(this::getNameByUUID).toList();
        if (members.isEmpty()) return ColorUtils.chat(Alert.XmasLegacy + " 파티원이 없네요..? 뭔가 이상해보입니다.");

        Component msgLeader = ColorUtils.chat(Alert.YELLOW + " 파티 리더 : &6" + getNameByUUID(party.getLeader()));
        Component msgMembers = ColorUtils.chat(Alert.YELLOW + " 파티 멤버 : &6" + (notLeader.isEmpty() ? "&c없음" : String.join(", ", notLeader)));
        return msgLeader.appendNewline().append(msgMembers);
    }
}
