package org.lazberry.xmaslegacy.rank.board;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.lazberry.xmaslegacy.rank.RankManager;
import org.lazberry.xmaslegacy.rank.RankType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class RankBoard {
	private final RankType rankType;
	private final Location location;
	private final int printLimit;
	private TextDisplay existingDisplay;

	private final Map<UUID, Integer> previousRanks = new HashMap<>();

	public RankBoard(RankType rankType, Location location, int printLimit) {
		this.rankType = rankType;
		this.location = location;
		this.printLimit = printLimit;
	}

	private String getTitleName(RankType type) {
		return switch (type) {
			case DOLLAR -> "자산 랭킹";
			case EXP -> "경험치 랭킹";
			case ROLE_EXP -> "직업 경험치 랭킹";
			case PLAYTIME -> "플레이 시간 랭킹";
		};
	}

	private Color getGlowColor(RankType type) {
		return switch (type) {
			case DOLLAR -> Color.ORANGE;
			case EXP -> Color.GREEN;
			case ROLE_EXP -> Color.RED;
			case PLAYTIME -> Color.AQUA;
		};
	}

	public TextDisplay spawnOrUpdateDisplay(ArrayList<RankManager.RankData> ranks) {
		if (existingDisplay == null || !existingDisplay.isValid()) {
			existingDisplay = location.getWorld().spawn(location, TextDisplay.class, t -> {
				t.setGlowColorOverride(getGlowColor(rankType));
				t.setGlowing(true);
				t.setAlignment(TextDisplay.TextAlignment.CENTER);
				t.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
			});
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<gold><b>[ ").append(getTitleName(rankType)).append(" ]</b></gold>\n");
		sb.append("<dark_gray>--------------------</dark_gray>\n");

		Map<UUID, Integer> currentRanks = new HashMap<>();
		int count = 0;

		for (RankManager.RankData data : ranks) {
			if (count >= printLimit) break;

			int currentRank = data.rank();
			UUID uuid = data.uuid();
			double value = data.value();

			String name = Bukkit.getOfflinePlayer(uuid).getName();
			if (name == null) name = "Unknown";

			String trendSymbol;
			if (!previousRanks.containsKey(uuid)) {
				trendSymbol = "<gray>-</gray>";
			} else {
				int prevRank = previousRanks.get(uuid);
				if (currentRank < prevRank) {
					trendSymbol = "<red>▲</red>";
				} else if (currentRank > prevRank) {
					trendSymbol = "<blue>▼</blue>";
				} else {
					trendSymbol = "<green>-</green>";
				}
			}

			currentRanks.put(uuid, currentRank);
			sb.append(String.format("<white>%d위. <yellow>%s</yellow> <dark_gray>-</dark_gray> <aqua>%.1f</aqua> %s</white>\n",
					currentRank, name, value, trendSymbol));

			count++;
		}

		previousRanks.clear();
		previousRanks.putAll(currentRanks);

		Component component = MiniMessage.miniMessage().deserialize(sb.toString().trim());
		existingDisplay.text(component);

		return existingDisplay;
	}

	public void removeDisplay() {
		if (existingDisplay != null && existingDisplay.isValid()) {
			existingDisplay.remove();
			existingDisplay = null;
		}
	}
}
