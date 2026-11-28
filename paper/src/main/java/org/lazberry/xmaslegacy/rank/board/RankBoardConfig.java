package org.lazberry.xmaslegacy.rank.board;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.rank.RankType;
import org.lazberry.xmaslegacy.utils.ParseEnum;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
  * rank_board.yml. Positions are plain values, not serialized Locations: a Location whose world is not loaded yet
  * is read as null, and the next save would then erase that board.
 */
@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class RankBoardConfig {
	private final File file;

	@Inject
	public RankBoardConfig(XmasLegacy plugin) {
		this.file = new File(plugin.getDataFolder(), "rank_board.yml");
	}

	public boolean save(@NotNull Map<RankType, RankBoard> boards) {
		var config = new YamlConfiguration();
		boards.forEach((type, board) -> {
			String path = type.name() + ".";
			config.set(path + "world", board.getWorld());
			config.set(path + "x", board.getX());
			config.set(path + "y", board.getY());
			config.set(path + "z", board.getZ());
			config.set(path + "print-limit", board.getPrintLimit());
		});
		try {
			config.save(file);
			return true;
		} catch (IOException e) {
			log.error("Failed to save rank boards.", e);
			return false;
		}
	}

	public @NotNull Map<RankType, RankBoard> load() {
		Map<RankType, RankBoard> result = new EnumMap<>(RankType.class);
		if (!file.exists()) return result;

		var config = YamlConfiguration.loadConfiguration(file);
		for (String key : config.getKeys(false)) {
			RankType type = ParseEnum.of(RankType.class).parse(key);
			String world = config.getString(key + ".world");
			if (type == null || world == null) {
				log.error("Unreadable rank board '{}', skipped. Place it again with /board spawn.", key);
				continue;
			}
			result.put(type, new RankBoard(type, world, config.getDouble(key + ".x"), config.getDouble(key + ".y"),
					config.getDouble(key + ".z"), config.getInt(key + ".print-limit", 10)));
		}
		return result;
	}
}
