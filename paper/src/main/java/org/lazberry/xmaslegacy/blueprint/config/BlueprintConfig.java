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
	private final File schematicsFolder;

	@Inject
	public BlueprintConfig(XmasLegacy plugin) {
		this.schematicsFolder = new File(plugin.getDataFolder(), "blueprints");
	}

	public CompletableFuture<Boolean> saveAsync(BluePrint bluePrint) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				File folder = new File(schematicsFolder, "blueprints");
				if (!folder.exists()) folder.mkdirs();

				File file = new File(folder, bluePrint.getStructureName() + ".json");

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

		if (!schematicsFolder.exists()) {
			schematicsFolder.mkdirs();
			return result;
		}

		File[] files = schematicsFolder.listFiles((dir, name) -> name.endsWith(".schem") || name.endsWith(".schematic"));
		if (files != null) {
			for (File file : files) {
				String id = file.getName().substring(0, file.getName().lastIndexOf('.'));
				BluePrint bp = BlueprintSchematicManager.loadAndCreateBlueprint(file, id, 0f);
				if (bp != null) {
					result.put(id, bp);
				}
			}
		}
		return result;
	}

	public CompletableFuture<Void> deleteAsync(String id) {
		return CompletableFuture.runAsync(() -> {
			File file = new File(schematicsFolder, id + ".schem");
			if (!file.exists()) {
				file = new File(schematicsFolder, id + ".schematic");
			}
			if (file.exists() && !file.delete()) {
				log.warn("Failed to delete schematic file: {}", file.getName());
			}
		});
	}

	public File getSchematicFile(String id) {
		return new File(schematicsFolder, id + ".schem");
	}
}
