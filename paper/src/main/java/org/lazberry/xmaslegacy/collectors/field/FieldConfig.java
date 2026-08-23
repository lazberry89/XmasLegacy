package org.lazberry.xmaslegacy.collectors.field;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.ParseEnum;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;
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

	public void saveConfig() {
		try {
			config.save(file);
		} catch (IOException e) {
            log.error("Could not save collectors_fields.yml file.", e);
        }
	}

    public synchronized void saveSync() {
		var builder = ConfigBuilder.of(config);
		fm.forEach(f -> {
			String diffPath = path + f.getDifficulty();
			config = builder.set(diffPath + "spawn", f.getSpawn())
					.set(diffPath + "pos1", f.getPos1())
					.set(diffPath + "pos2", f.getPos2())
					.set(diffPath + "drop-locations", f.getPotentialDropLocations())
					.save(file)
					.build();
		});
	}

	public CompletableFuture<Void> save() {
		return CompletableFuture.runAsync(this::saveSync);
	}

	public synchronized void loadSync() {
		ConfigurationSection difficultySection = config.getConfigurationSection(path);
		if (difficultySection == null) {
			log.error("Section is not detected. No field is available.");
			return;
		}

		for (String difficulty : difficultySection.getKeys(true)) {
			Difficulty resultDifficulty = ParseEnum.of(Difficulty.class).parse(difficulty);
			if (resultDifficulty == null) {
				log.error("No such difficulty value of {}.", difficulty);
				return;
			}

		}
	}

	@Override
	public void close() {
		Initiator.super.close();
	}
}
