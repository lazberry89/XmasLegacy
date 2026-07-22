package org.lazberry.xmaslegacy.Ranks.RankingCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.Ranks.RankingTask;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;
import org.lazberry.xmaslegacy.XmasLegacy;

public class RankingCommandTask implements SubCommand {
	private final @NotNull XmasLegacy plugin;
	private final @NotNull RankingTask ranking;

	public RankingCommandTask(@NotNull XmasLegacy plugin, @NotNull RankingTask ranking) {
		this.plugin = plugin;
		this.ranking = ranking;
	}

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 3) {
			InfoUtils.error(player, "유효하지 않은 명령어입니다!");
			return;
		}
		if (args[1].equalsIgnoreCase("all")) {
			if (args[2].equalsIgnoreCase("on")) {
				this.ranking.startTask(plugin);
				InfoUtils.warn(player, "모든 랭킹 테스크를 시작하였습니다.");
			} else if (args[2].equalsIgnoreCase("off")) {
				this.ranking.stopTask();
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
				this.ranking.startRankTask(plugin, type);
				InfoUtils.info(player, String.format("&5%s&f&r타입의 테스크를 시작했습니다.", type));
			} else if (args[2].equalsIgnoreCase("off")) {
				this.ranking.stopRankTask(type);
				InfoUtils.info(player, String.format("&5%s&f&r타입의 테스크를 종료했습니다.", type));
			} else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
		}
	}
}
