package org.lazberry.xmaslegacy.rank.board;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.rank.RankType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;
import org.lazberry.xmaslegacy.utils.ParseEnum;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class RankBoardConfig implements Initiator {
	private final File dataFolder;
	private @Getter YamlConfiguration config;
	private File file;

	@Inject
	public RankBoardConfig(XmasLegacy plugin) {
		this.dataFolder = plugin.getDataFolder();
	}

	@Override
	public void init() {
		file = new File(dataFolder, "rank_board.yml");

		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			log.error("Failed to create directories for rank board.");
			return;
		}

		if (!file.exists()) {
			try {
				if (file.createNewFile()) {
					log.info("Successfully created rank board files.");
				}
			} catch (IOException e) {
				log.error("Exception occurred while initiating rank board files.", e);
			}
		}
		this.config = YamlConfiguration.loadConfiguration(file);
	}

	public void saveSync(Map<RankType, RankBoard> values) {
		synchronized (this) {
			var builder = ConfigBuilder.create();
			for (var entry : values.entrySet()) {
				String path = entry.getKey().name() + ".";
				RankBoard board = entry.getValue();

				builder.set(path + "location", board.getLocation());
				builder.set(path + "print-limit", board.getPrintLimit());
			}
			config = builder.save(file).build();
		}
	}

	public Map<RankType, RankBoard> loadSync() {
		synchronized (this) {
			config = YamlConfiguration.loadConfiguration(file);
			Map<RankType, RankBoard> result = new HashMap<>();

			var section = config.getKeys(false);
			if (section.isEmpty()) {
				log.warn("No rank board configuration found. Skipping...");
				return new HashMap<>();
			}
			for (String key : section) {
				RankType type = ParseEnum.of(RankType.class).parse(key);
				Location loc = config.getLocation(key + ".location");
				int limit = config.getInt(key + ".print-limit", 1);

				if (type == null || loc == null) {
					log.error("Failed to parse Ranking board from configuration. Skipping...");
					continue;
				}
				RankBoard board = new RankBoard(type, loc, limit);
				result.put(type, board);
			}
			return result;
		}
	}

	public CompletableFuture<Void> saveAsync(Map<RankType, RankBoard> values) {
		return CompletableFuture.runAsync(() -> saveSync(values));
	}

	public CompletableFuture<Map<RankType, RankBoard>> loadAsync() {
		return CompletableFuture.supplyAsync(this::loadSync);
	}
}
