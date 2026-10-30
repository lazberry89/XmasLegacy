package org.lazberry.xmaslegacy.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Getter @Setter
public abstract class AbstractDataSerializer<V> implements Initiator {
    private final XmasLegacy plugin;
    private final File dataFolder;
    private final String name;
    private YamlConfiguration config;
    private File file;

    public AbstractDataSerializer(XmasLegacy plugin, String file) {
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
                plugin.saveResource(name + ".yml", false);
                log.info("Successfully loaded default {} file from resources.", name);
            } catch (IllegalArgumentException e) {
                try {
                    if (file.createNewFile()) {
                        log.info("Successfully created empty {} files.", name);
                    }
                } catch (IOException ex) {
                    log.error("Exception occurred while initiating {} files.", name, ex);
                }
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        abstractInitiate();
    }

    public void abstractInitiate() {}
    public abstract void saveSync(V value);
    public abstract V loadSync();

    public CompletableFuture<Void> saveAsync(V value) {
        return CompletableFuture.runAsync(() -> saveSync(value));
    }

    public CompletableFuture<V> loadAsync() {
        return CompletableFuture.supplyAsync(this::loadSync);
    }
}
