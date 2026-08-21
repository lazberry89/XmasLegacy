package org.lazberry.xmaslegacy.ranks.RankingCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ranks.RankManager;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.utils.UserHandler;

public class RankingCommandSelf implements SubCommand {
	private final @NotNull UserManager um;
	private final @NotNull RankManager rm;

	public RankingCommandSelf(@NotNull UserManager um, @NotNull RankManager rm) {
		this.um = um;
		this.rm = rm;
	}

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 2) {
			InfoUtils.error(player, "잘못된 사용법입니다!");
			return;
		}
		var user = um.getUser(player.getUniqueId());
		if (user == null) {
			UserHandler.sendReloadNotice(player);
			return;
		}
		RankType type;
		try {
			type = RankType.valueOf(args[1].toUpperCase());
		} catch (IllegalArgumentException e) {
			InfoUtils.error(player, "존재하지 않는 랭크 타입입니다.");
			return;
		}
		int rank = rm.getRank(type, user);
		if (rank == -1) {
			InfoUtils.error(player, "유효한 랭킹 범위 밖입니다! (100등 미만)");
			return;
		}
		InfoUtils.info(player, "현재 유저님의 &5" + type + "&f카테고리의 랭크는 &6" + rank + "등 &f입니다.");
	}
}
