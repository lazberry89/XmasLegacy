package org.lazberry.xmaslegacy.collectors.field;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.utils.ParseEnum;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class FieldConfig implements Initiator {
	private final FieldManager fm;
	private final File dataFolder;
	private final String path = "fields";
	private @Getter YamlConfiguration config;
	private File file;

	@Inject
	public FieldConfig(FieldManager fm, XmasLegacy plugin) {
        this.fm = fm;
        this.dataFolder = plugin.getDataFolder();
	}

	@Override
	public void init() {
		file = new File(dataFolder, "collectors_fields.yml");

		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			log.error("Failed to create directories for collectors_fields.");
			return;
		}

		boolean isNewFile = false;
		if (!file.exists()) {
			try {
				isNewFile = file.createNewFile();
			} catch (IOException e) {
				log.error("Could not create collectors_fields.yml", e);
			}
		}

		this.config = YamlConfiguration.loadConfiguration(file);

		if (isNewFile) saveSync();
		else loadSync();
	}

    public synchronized void saveSync() {
		var builder = ConfigBuilder.of(config);
		fm.forEach(f -> {
			String diffPath = path + "." + f.getDifficulty().name() + ".";
			builder.set(diffPath + "spawn", f.getSpawn())
					.set(diffPath + "pos1", f.getPos1())
					.set(diffPath + "pos2", f.getPos2())
					.set(diffPath + "drop-locations", f.getPotentialDropLocations());
		});
		this.config = builder.save(file).build();
	}

	public CompletableFuture<Void> save() {
		return CompletableFuture.runAsync(this::saveSync);
	}

	public synchronized void loadSync() {
		fm.clear();

		ConfigurationSection difficultySection = config.getConfigurationSection(path);
		if (difficultySection == null) {
			log.error("Section is not detected. No field is available.");
			return;
		}

		for (String difficultyStr : difficultySection.getKeys(false)) {
			Difficulty difficulty = ParseEnum.of(Difficulty.class).parse(difficultyStr);
			if (difficulty == null) {
				log.error("No such difficulty value of {}.", difficultyStr);
				continue;
			}
			String diffPath = path + "." + difficultyStr + ".";

			Location pos1 = config.getLocation(diffPath + "pos1");
			Location pos2 = config.getLocation(diffPath + "pos2");
			Location spawn = config.getLocation(diffPath + "spawn");
			if (pos1 == null || pos2 == null || spawn == null) {
				log.error("Field locations for Difficulty {} is Invalid.", difficultyStr);
				continue;
			}

			Field field = new Field(difficulty, pos1, pos2, spawn);

            List<?> rawDropLocs = config.getList(diffPath + "drop-locations");
			if (rawDropLocs != null) {
				rawDropLocs.stream()
						.filter(Location.class::isInstance)
						.map(Location.class::cast)
						.forEach(field::addDropLocation);
			}

			fm.registerField(field);
		}
	}

	public CompletableFuture<Void> load() {
		return CompletableFuture.runAsync(this::loadSync);
	}

	@Override
	public void close() {
		saveSync();
		fm.clear();
	}
}
