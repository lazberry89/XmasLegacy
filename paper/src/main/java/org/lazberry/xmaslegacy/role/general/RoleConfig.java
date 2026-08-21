package org.lazberry.xmaslegacy.role.general;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = ServerType.WILD)
public class RoleConfig implements Initiator {
    private final RoleManager rm;
    private final File dataFolder;
    private File file;
    private @Getter YamlConfiguration config;

    @Inject
    public RoleConfig(RoleManager rm, XmasLegacy plugin) {
        this.rm = rm;
        this.dataFolder = plugin.getDataFolder();
    }

    @Override
    public void init() {
        file = new File(dataFolder, "roles.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for roles.yml");
            return;
        }

        boolean isNewFile = false;
        if (!file.exists()) {
            try {
                isNewFile = file.createNewFile();
            } catch (IOException e) {
                log.error("Could not create roles.yml", e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        if (isNewFile) {
            applyDefaults();
        }
        loadSync();
    }

    private void applyDefaults() {
        config = ConfigBuilder.of(file)
                .setDefault("farmer.additional-drops.min", 1)
                .setDefault("farmer.additional-drops.max", 3)
                .setDefault("farmer.additional-exp.min", 10)
                .setDefault("farmer.additional-exp.max", 30)
                .save(file)
                .build();
    }

    public synchronized void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            log.error("Could not save roles.yml to {}", file, e);
        }
    }

    public void saveSync() {
        int dropsMin = rm.farmer().getAdditionalDropsMin();
        int dropsMax = rm.farmer().getAdditionalDropsMax();
        int expMin = rm.farmer().getAdditionalExpMin();
        int expMax = rm.farmer().getAdditionalExpMax();

        synchronized (this) {
            config.set("farmer.additional-drops.min", dropsMin);
            config.set("farmer.additional-drops.max", dropsMax);
            config.set("farmer.additional-exp.min", expMin);
            config.set("farmer.additional-exp.max", expMax);
            saveConfig();
        }
    }

    public CompletableFuture<Void> save() {
        return CompletableFuture.runAsync(this::saveSync);
    }

    public void loadSync() {
        synchronized (this) {
            this.config = YamlConfiguration.loadConfiguration(file);

            rm.farmer().setAdditionalDropsMin(config.getInt("farmer.additional-drops.min", 1));
            rm.farmer().setAdditionalDropsMax(config.getInt("farmer.additional-drops.max", 3));
            rm.farmer().setAdditionalExpMin(config.getInt("farmer.additional-exp.min", 10));
            rm.farmer().setAdditionalExpMax(config.getInt("farmer.additional-exp.max", 30));
        }
    }

    public CompletableFuture<Void> load() {
        return CompletableFuture.runAsync(this::loadSync);
    }

    @Override
    public void close() {
        saveSync();
    }
}
