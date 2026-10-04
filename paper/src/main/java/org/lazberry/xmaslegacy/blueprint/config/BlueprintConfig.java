package org.lazberry.xmaslegacy.blueprint.config;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintConfig {
	private final File blueprintFolder;

	@Inject
	public BlueprintConfig(XmasLegacy plugin) {
		this.blueprintFolder = new File(plugin.getDataFolder(), "blueprints");
	}

	public CompletableFuture<Boolean> saveAsync(BluePrint bluePrint) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (!blueprintFolder.exists()) blueprintFolder.mkdirs();

				File file = new File(blueprintFolder, bluePrint.getStructureName() + ".json");

				String json = bluePrint.toJson();
				Files.writeString(file.toPath(), json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				return true;
			} catch (Exception e) {
				log.error("Failed to save blueprint file: {}", bluePrint.getStructureName(), e);
				return false;
			}
		});
	}

	public Map<String, BluePrint> loadSync() {
		Map<String, BluePrint> result = new HashMap<>();

		if (!blueprintFolder.exists()) {
			blueprintFolder.mkdirs();
			return result;
		}

		File[] files = blueprintFolder.listFiles((dir, name) -> name.endsWith(".json"));
		if (files != null) {
			for (File file : files) {
				try {
					String id = file.getName().substring(0, file.getName().lastIndexOf('.'));
					String json = Files.readString(file.toPath());

					BluePrint bp = BluePrint.parseInstanceFromJson(json);
					if (bp != null) {
						result.put(id, bp);
					}
				} catch (Exception e) {
					log.error("Failed to load blueprint json file: {}", file.getName(), e);
				}
			}
		}
		return result;
	}

	public CompletableFuture<Void> deleteAsync(String id) {
		return CompletableFuture.runAsync(() -> {
			File file = new File(blueprintFolder, id + ".json");
			if (file.exists() && !file.delete()) {
				log.warn("Failed to delete blueprint json file: {}", file.getName());
			}
		});
	}

	public File getBlueprintFile(String id) {
		return new File(blueprintFolder, id + ".json");
	}
}
