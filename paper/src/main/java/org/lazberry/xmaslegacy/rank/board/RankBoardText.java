package org.lazberry.xmaslegacy.rank.board;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.rank.RankManager;
import org.lazberry.xmaslegacy.rank.RankType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/** What a rank board shows. No server access in here. */
final class RankBoardText {
	private static final String LINE = "<#2B4A6F><st>                                            </st></#2B4A6F>\n";
	private static final String[] MEDALS = {"#FFD34E", "#D5DEE8", "#E09A5B"};

	private RankBoardText() {}

	private record Theme(String title, String subtitle, String from, String to) {}

	private static Theme theme(RankType type) {
		return switch (type) {
			case DOLLAR -> new Theme("자산 랭킹", "얼어붙은 세계의 부호들", "#FFE08A", "#FF9F1C");
			case EXP -> new Theme("레벨 랭킹", "가장 멀리 나아간 모험가들", "#B8F59B", "#2FBF71");
			case ROLE_EXP -> new Theme("직업 랭킹", "한 길을 걸어온 장인들", "#FFB3B3", "#E5484D");
			case PLAYTIME -> new Theme("플레이 시간 랭킹", "설원을 지켜온 시간", "#9BE8FF", "#4A7DFF");
		};
	}

	static @NotNull Component render(@NotNull RankType type, @NotNull List<RankManager.RankData> ranks,
	                                 @NotNull Map<UUID, Integer> baseline, @NotNull Function<UUID, String> names) {
		Theme theme = theme(type);
		StringBuilder sb = new StringBuilder("\n<white>❄</white> <b><gradient:")
				.append(theme.from).append(':').append(theme.to).append('>').append(theme.title)
				.append("</gradient></b> <white>❄</white>\n<#7F93AD>").append(theme.subtitle).append("</#7F93AD>\n").append(LINE);

		if (ranks.isEmpty()) sb.append("<gray>아직 기록이 없습니다.</gray>\n");
		for (RankManager.RankData data : ranks) {
			int rank = data.rank();
			String color = rank <= MEDALS.length ? MEDALS[rank - 1] : "#8FA3BD";
			String name = MiniMessage.miniMessage().escapeTags(names.apply(data.uuid()));
			sb.append('<').append(color).append('>').append(rank <= MEDALS.length ? "<b>" + rank + "</b> ✦" : rank + ".")
					.append("</").append(color).append("> ")
					.append(rank <= MEDALS.length ? "<white>" + name + "</white>" : "<#C9D4E3>" + name + "</#C9D4E3>")
					.append("  <").append(theme.to).append('>').append(format(type, data.value())).append("</").append(theme.to).append("> ")
					.append(trend(baseline, data)).append('\n');
		}
		sb.append(LINE).append("<#5C7391>변동은 10분 기준  ·  XmasLegacy</#5C7391>\n");
		return MiniMessage.miniMessage().deserialize(sb.toString());
	}

	private static String trend(Map<UUID, Integer> baseline, RankManager.RankData data) {
		if (baseline.isEmpty()) return "<#5C7391>-</#5C7391>";
		Integer before = baseline.get(data.uuid());
		if (before == null) return "<#FFD34E>NEW</#FFD34E>";
		int moved = before - data.rank();
		if (moved > 0) return "<#FF6B6B>▲" + moved + "</#FF6B6B>";
		if (moved < 0) return "<#6BA8FF>▼" + -moved + "</#6BA8FF>";
		return "<#5C7391>-</#5C7391>";
	}

	static @NotNull String format(@NotNull RankType type, double value) {
		long v = (long) value;
		return switch (type) {
			case DOLLAR -> String.format("%,d원", v);
			case EXP -> "Lv." + RankManager.levelOf(v);
			case ROLE_EXP -> String.format("%,d XP", v);
			case PLAYTIME -> {
				long minutes = v / 20 / 60; // ticks
				yield minutes < 60 ? minutes + "분" : String.format("%,d시간 %d분", minutes / 60, minutes % 60);
			}
		};
	}
}
