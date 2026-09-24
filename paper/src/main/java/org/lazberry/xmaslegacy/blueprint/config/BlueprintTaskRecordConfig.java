package org.lazberry.xmaslegacy.blueprint.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.blueprint.BluePrintManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintTaskRecordConfig implements Initiator {
	private final File dataFolder;
	private @Getter YamlConfiguration config;
	private File file;

	@Inject
	public BlueprintTaskRecordConfig(XmasLegacy plugin) {
		this.dataFolder = plugin.getDataFolder();
	}

	@Override
	public void init() {
		file = new File(dataFolder, "build_task.yml");

		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			log.error("Failed to create directories for build task.");
			return;
		}

		if (!file.exists()) {
			try {
				if (file.createNewFile()) {
					log.info("Successfully created build task files.");
				}
			} catch (IOException e) {
				log.error("Exception occurred while initiating build task files.", e);
			}
		}
		this.config = YamlConfiguration.loadConfiguration(file);
		ConfigBuilder.of(file).setDefault("task_delay", 5);
	}

	public long getTaskDelay() {
		return config.getLong("task_delay", 5L);
	}

	public void saveSync(Map<UUID, BluePrintManager.TaskRecord> records) {
		synchronized (this) {
			var builder = ConfigBuilder.of(file);
			builder.set("tasks", null);

			for (var record : records.values()) {
				UUID uuid = record.builder();
				String path = "tasks." + uuid.toString();

				builder.set(path + ".structure_name", record.structureName());
				builder.set(path + ".process", record.process());
				builder.set(path + ".start_location", record.startLocation());
				builder.set(path + ".elapsed_millis", record.elapsedMillis());
			}
			this.config = builder.save(file).build();
		}
	}

	public CompletableFuture<Void> saveAsync(Map<UUID, BluePrintManager.TaskRecord> records) {
		return CompletableFuture.runAsync(() -> saveSync(records));
	}

	public Map<UUID, BluePrintManager.TaskRecord> loadSync() {
		synchronized (this) {
			Map<UUID, BluePrintManager.TaskRecord> records = new ConcurrentHashMap<>();
			if (config == null) return records;

			var section = config.getConfigurationSection("tasks");
			if (section == null) return records;

			for (String key : section.getKeys(false)) {
				UUID uuid = UUID.fromString(key);
				String path = "tasks." + uuid;

				String name = config.getString(path + ".structure_name");
				int process = config.getInt(path + ".process");
				Location startLocation = config.getLocation(path + ".start_location");
				long elapsedMillis = config.getLong(path + ".elapsed_millis");

				if (name == null || startLocation == null) {
					log.warn("Missing or invalid value for key {}", path);
					continue;
				}
				BluePrintManager.TaskRecord record = new BluePrintManager.TaskRecord(uuid, name, process, startLocation, elapsedMillis);
				records.put(uuid, record);
			}
			return records;
		}
	}

	public CompletableFuture<Map<UUID, BluePrintManager.TaskRecord>> loadAsync() {
		return CompletableFuture.supplyAsync(this::loadSync);
	}
}
