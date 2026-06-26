package xmaslegacy.Ranks.RankingCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Ranks.RankingSystem;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.ServerTransfer;
import xmaslegacy.Utils.SubCommand;

public class RankingCommandSelf implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 2) {
			InfoUtils.error(player, "잘못된 사용법입니다!");
			return;
		}
		var user = UserManager.INSTANCE.getUser(player.getUniqueId());
		if (user == null) {
			ServerTransfer.sendReloadNotice(player);
			return;
		}
		RankType type;
		try {
			type = RankType.valueOf(args[1].toUpperCase());
		} catch (IllegalArgumentException e) {
			InfoUtils.error(player, "존재하지 않는 랭크 타입입니다.");
			return;
		}
		int rank = RankingSystem.INSTANCE.getRank(type, user);
		if (rank == -1) {
			InfoUtils.error(player, "유효한 랭킹 범위 밖입니다! (100등 미만)");
			return;
		}
		InfoUtils.info(player, "현재 유저님의 &5" + type + "&f카테고리의 랭크는 &6" + rank + "등 &f입니다.");
	}
}
