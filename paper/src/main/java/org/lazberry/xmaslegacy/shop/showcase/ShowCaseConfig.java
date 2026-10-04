package org.lazberry.xmaslegacy.shop.showcase;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.shop.goods.Goods;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;
import org.lazberry.xmaslegacy.utils.ParseEnum;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class ShowCaseConfig implements Initiator {
	private final File dataFolder;
	private @Getter YamlConfiguration config;
	private File file;

	@Inject
	public ShowCaseConfig(XmasLegacy plugin) {
		this.dataFolder = plugin.getDataFolder();
	}

	@Override
	public void init() {
		file = new File(dataFolder, "showcase.yml");

		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			log.error("Failed to create directories for showcase.");
			return;
		}

		if (!file.exists()) {
			try {
				if (file.createNewFile()) {
					log.info("Successfully created showcase files.");
				}
			} catch (IOException e) {
				log.error("Exception occurred while initiating showcase files.", e);
			}
		}
		this.config = YamlConfiguration.loadConfiguration(file);
	}

	public void saveSync(Map<String, ShowCase> showcases) {
		synchronized (this) {
			var builder = ConfigBuilder.create();
			for (var s : showcases.values()) {
				if (s.getBaseLocation() == null || s.getFace() == null) continue;

				String path = "showcases." + s.getGoods().getName() + ".";
				Location loc = s.getBaseLocation();
				builder.set(path + "location", loc);
				builder.set(path + "face", s.getFace().name());
			}
			this.config = builder.save(file).build();
		}
	}

	public Map<String, ShowCase> loadSync(Map<String, Goods> goodsMap) {
		synchronized (this) {
			ConfigurationSection section = this.config.getConfigurationSection("showcases");
			if (section == null) return new HashMap<>();

			Map<String, ShowCase> result = new HashMap<>();
			for (var s : section.getKeys(false)) {
				String path = "showcases." + s + ".";

				Location loc = config.getLocation(path + "location");
				BlockFace face = ParseEnum.of(BlockFace.class).parse(config.getString(path + "face"));
				Goods goods = goodsMap.get(s);
				if (loc == null || face == null || goods == null) {
					log.error("Failed to load showcase section {}.", s);
					continue;
				}
				ShowCase showCase = new ShowCase(goods, loc, face);
				var chunk = loc.getChunk();
				if (!chunk.isLoaded()) chunk.load();
				showCase.spawnShowcase(loc);
				showCase.spawnWallButton(loc, face);

				result.put(s, showCase);
			}
			return result;
		}
	}

	public CompletableFuture<Void> saveAsync(Map<String, ShowCase> values) {
		return CompletableFuture.runAsync(() -> saveSync(values));
	}
}
