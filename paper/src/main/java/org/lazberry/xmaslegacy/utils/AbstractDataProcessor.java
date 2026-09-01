package org.lazberry.xmaslegacy.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public abstract class AbstractDataProcessor implements Initiator {
    private final XmasLegacy plugin;
    private final File dataFolder;
    private final String name;
    private YamlConfiguration config;
    private File file;

    public AbstractDataProcessor(XmasLegacy plugin, String file) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.name = file;
    }

    @Override
    public void init() {
        file = new File(dataFolder, name + ".yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for {}.", name);
            return;
        }

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    log.info("Successfully created {} files.", name);
                }
            } catch (IOException e) {
                log.error("Exception occurred while initiating {} files.", name, e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadSync();
        abstractInitiate();
    }

    @Override
    public void close() {
        saveSync();
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            log.error("Failed to save {} file.", name);
        }
    }

    public abstract void abstractInitiate();
    public abstract void saveSync();
    public abstract void loadSync();
    public CompletableFuture<Void> save(Object value) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Method is not overridden!"));
    }
    public CompletableFuture<Void> load(Object value) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Method is not overridden!"));
    }
    public CompletableFuture<Void> saveAll() {
        return CompletableFuture.runAsync(this::saveSync);
    }
    public CompletableFuture<Void> loadAll() {
        return CompletableFuture.runAsync(this::loadSync);
    }
}
