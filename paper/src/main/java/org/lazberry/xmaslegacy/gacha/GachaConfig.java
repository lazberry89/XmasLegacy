package org.lazberry.xmaslegacy.gacha;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lazberry.xmaslegacy.utils.ParseEnum;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class GachaConfig implements Initiator {
    private final GachaManager gm;
    private final File dataFolder;
    private @Getter YamlConfiguration config;
    private File file;

    @Inject
    public GachaConfig(GachaManager gm, XmasLegacy plugin) {
        this.gm = gm;
        this.dataFolder = plugin.getDataFolder();
    }

    @Override
    public void init() {
        file = new File(dataFolder, "gacha-settings.yml");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            log.error("Failed to create directories for gacha settings.");
            return;
        }

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    log.info("Successfully created gacha setting files.");
                }
            } catch (IOException e) {
                log.error("Exception occurred while initiating gacha setting files.", e);
            }
        }

        createDefaultConfig();
        load().join();
    }

    public void applySettings() {
        var builder = ConfigBuilder.of(config);
        var parser = ParseEnum.of(Material.class);
        gm.setBundle(parser.parse(builder.getValue("general-bundle", "BUNDLE")));
        gm.setHigh_end(parser.parse(builder.getValue("high-bundle", "CHEST")));
        gm.setChromatic_bundle(parser.parse(builder.getValue("chromatic-bundle", "ENDER_CHEST")));
        gm.setChromatic_box(parser.parse(builder.getValue("chromatic-box", "DRAGON_EGG")));
    }

    private void createDefaultConfig() {
        config = ConfigBuilder.of(file)
                .setDefault("general-bundle", "BUNDLE")
                .setDefault("high-bundle", "CHEST")
                .setDefault("chromatic-bundle", "ENDER_CHEST")
                .setDefault("chromatic-box", "DRAGON_EGG")
                .save(file)
                .build();
    }

    public CompletableFuture<Void> load() {
        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                applySettings();

            }
        });
    }
}
