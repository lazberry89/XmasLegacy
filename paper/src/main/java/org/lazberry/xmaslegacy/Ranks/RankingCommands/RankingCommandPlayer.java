package org.lazberry.xmaslegacy.Ranks.RankingCommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.Ranks.RankingSystem;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;

public class RankingCommandPlayer implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 2) {
			InfoUtils.error(player, "유효하지 않은 명령어입니다!");
			return;
		}
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			InfoUtils.error(player, "유효하지 않은 플레이어입니다!");
			return;
		}
		User user = UserManager.INSTANCE.getUser(target.getUniqueId());
		if (user == null) {
			InfoUtils.error(player, "유저가 오프라인 이거나 정보가 로드되지 않았습니다!");
			return;
		}
		RankType type;
		try {
			type = RankType.valueOf(args[1].toUpperCase());
		} catch (IllegalArgumentException e) {
			InfoUtils.error(player, "유효하지 않은 랭크 타입입니다.");
			return;
		}
		int rank = RankingSystem.INSTANCE.getRank(type, user);
		if (rank == -1) {
			InfoUtils.error(player, "유효한 랭킹 범위 밖입니다! (100등 미만)");
			return;
		}
		InfoUtils.info(player, "&6&l" + target.getName() +"&f&r님의 &5" + type + "&f카테고리의 랭크는 &6" + rank + "등 &f입니다.");
	}
}
