package xmaslegacy.Ranks.RankingCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.RankType;
import xmaslegacy.Ranks.RankingSystem;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

public class RankingCommandTask implements SubCommand {
	private final @NotNull RankingSystem ranking;

	public RankingCommandTask() {
		this.ranking = RankingSystem.INSTANCE;
	}

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 3) {
			InfoUtils.error(player, "유효하지 않은 명령어입니다!");
			return;
		}
		if (args[1].equalsIgnoreCase("all")) {
			if (args[2].equalsIgnoreCase("on")) {
				this.ranking.startRankTask();
				InfoUtils.warn(player, "모든 랭킹 테스크를 시작하였습니다.");
			} else if (args[2].equalsIgnoreCase("off")) {
				this.ranking.stopRankTask();
				InfoUtils.warn(player, "모든 랭킹 테스크를 중지하였습니다.");
			}
		} else {
			RankType type;
			try {
				type = RankType.valueOf(args[1].toUpperCase());
			} catch (IllegalArgumentException e) {
				InfoUtils.error(player, "유효하지 않은 랭크 타입입니다.");
				return;
			}
			if (args[2].equalsIgnoreCase("on")) {
				this.ranking.startRankTask(type);
				InfoUtils.info(player, String.format("&5%s&f&r타입의 테스크를 시작했습니다.", type));
			} else if (args[2].equalsIgnoreCase("off")) {
				this.ranking.stopRankTask(type);
				InfoUtils.info(player, String.format("&5%s&f&r타입의 테스크를 종료했습니다.", type));
			} else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
		}
	}
}
