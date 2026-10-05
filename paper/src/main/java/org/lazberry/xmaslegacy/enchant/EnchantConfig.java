package org.lazberry.xmaslegacy.enchant;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class EnchantConfig implements Initiator {
	private final File dataFolder;
	private YamlConfiguration config;
	private File file;

	@Inject
	public EnchantConfig(XmasLegacy plugin) {
		this.dataFolder = plugin.getDataFolder();
	}

	@Override
	public void init() {
		file = new File(dataFolder, "enchant.yml");

		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			log.error("Failed to create directories for enchant.");
			return;
		}

		if (!file.exists()) {
			try {
				if (file.createNewFile()) {
					log.info("Successfully created enchant files.");
				}
			} catch (IOException e) {
				log.error("Exception occurred while initiating enchant files.", e);
			}
		}
		this.config = YamlConfiguration.loadConfiguration(file);
	}

	public void saveSync(Location location) {
		synchronized (this) {
			this.config = ConfigBuilder.of(config)
					.set("location", location)
					.save(file)
					.build();
		}
	}

	public Location loadSync() {
		synchronized (this) {
			return config.getLocation("location");
		}
	}
}
