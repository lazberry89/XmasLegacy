package org.lazberry.xmaslegacy.blueprint.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintConfig implements Initiator {
    private final File dataFolder;
    private @Getter YamlConfiguration config;
    private File file;

    @Inject
    public BlueprintConfig(XmasLegacy plugin) {
        this.dataFolder = plugin.getDataFolder();
    }

    @Override
    public void init() {
        file = new File(dataFolder, "blueprints.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for blueprints.");
            return;
        }

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    log.info("Successfully created blueprints files.");
                }
            } catch (IOException e) {
                log.error("Exception occurred while initiating blueprints files.", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveSync(Map<String, BluePrint> saves) {
        synchronized (this) {
            var builder = ConfigBuilder.create();
            for (BluePrint print : saves.values()) {
				if (print == null) continue;
                String path = print.getStructureName();
                builder.set(path, print.toJson());
            }
            this.config = builder.save(file).build();
        }
    }

	public CompletableFuture<Void> saveAsync(Map<String, BluePrint> saves) {
		return CompletableFuture.runAsync(() -> saveSync(saves));
	}

	public Map<String, BluePrint> loadSync() {
		final Map<String, BluePrint> loaded = new ConcurrentHashMap<>();
		if (config == null) return loaded;

		synchronized (this) {
			for (String key : config.getKeys(false)) {
				String json = config.getString(key);
				if (json == null || json.isEmpty()) continue;
				BluePrint print = BluePrint.parseInstanceFromJson(json);
				if (print != null) {
					loaded.put(key, print);
				}
			}
		}
		return loaded;
	}

	public CompletableFuture<Map<String, BluePrint>> loadAsync() {
		return CompletableFuture.supplyAsync(this::loadSync);
	}
}
