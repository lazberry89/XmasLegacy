package org.lazberry.xmaslegacy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = ServerType.GLOBAL)
public class RuleConfig implements Initiator {
    private final String path = "filters";
    private final RuleManager rm;
    private final File dataFolder;
    private @Getter YamlConfiguration config;
    private File file;

    @Inject
    public RuleConfig(XmasLegacy plugin, RuleManager rm) {
        this.dataFolder = plugin.getDataFolder();
        this.rm = rm;
    }

    @Override
    public void init() {
        file = new File(dataFolder, "filter_words.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for filter_words.");
            return;
        }

        boolean isNewFile = false;
        if (!file.exists()) {
            try {
                isNewFile = file.createNewFile();
            } catch (IOException e) {
                log.error("Could not create filter_words.yml", e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        if (isNewFile) saveSync();
        else load().join();
    }

    @Override
    public void close() {
        saveSync();
    }

    public synchronized void saveSync() {
        config.set(path, new ArrayList<>(rm.getBadWordList()));
        saveConfig();
    }

    public CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(this::saveSync);
    }

    public synchronized void loadSync() {
        List<String> list = config.getStringList(path);
        rm.addAll(list);
    }

    public CompletableFuture<Void> load() {
        return CompletableFuture.runAsync(this::loadSync);
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            log.error("Could not save rule filters to {}", file, e);
        }
    }
}
