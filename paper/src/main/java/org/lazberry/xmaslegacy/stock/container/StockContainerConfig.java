package org.lazberry.xmaslegacy.stock.container;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.Utils.InventorySerializer;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class StockContainerConfig implements Initiator {
    private final File root;
    private final StockContainerManager scm;
    private YamlConfiguration config;
    private File file;

    @Inject
    public StockContainerConfig(XmasLegacy plugin, StockContainerManager scm) {
        this.root = plugin.getDataFolder();
        this.scm = scm;
    }

    @Override
    public void init() {
        file = new File(root, "stock_containers.yml");
        try {
            if (!root.exists()) {
                if (!root.mkdirs()) {
                    log.error("Failed to create directories for stock containers.");
                    return;
                }
            }
            if (file.createNewFile()) {
                log.info("Successfully created stock setting files.");
                createDefaultConfig();
            }
        } catch (IOException e) {
            log.error("Exception occurred while initiating stock container files.", e);
        }

        config = YamlConfiguration.loadConfiguration(file);
        loadContainers().join();
    }

    private void createDefaultConfig() {
        config = YamlConfiguration.loadConfiguration(file);
        config.addDefault("type", "container");
		config.options().copyDefaults(true);
    }

    public CompletableFuture<Collection<StockContainer>> loadContainers() {
        ConfigurationSection stocksSection = config.getConfigurationSection("containers");
        if (stocksSection == null) return CompletableFuture.completedFuture(null);

        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
				List<StockContainer> containers = new ArrayList<>();
                for (String owner : stocksSection.getKeys(false)) {
                    try {
                        String path = "containers." + owner;
                        UUID uuid = UUID.fromString(owner);
                        ItemStack[] contents = InventorySerializer.deserializeContents(config.getString(path, ""));

                        StockContainer container = new StockContainer(uuid, contents);
                        containers.add(container);
                    } catch (Exception e) {
                        log.error("Failed to load itemStack information from Config. (UUID: {})", owner);
                    }
                }
				scm.addAll(containers);
				return containers;
            }
        });
    }

    public CompletableFuture<Void> saveContainers() {
        var containers = scm.getStockContainers();
        if (containers.isEmpty()) {
            log.warn("Containers are empty! skipping save process..");
            return CompletableFuture.completedFuture(null);
        }

        Map<UUID, ItemStack[]> snapshotMap = new HashMap<>();
        for (StockContainer container : containers) {
            snapshotMap.put(container.getOwner(), container.getContentsForSave());
        }

        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                try {
                    snapshotMap.forEach((owner, contents) -> {
                        String path = "containers." + owner.toString();
                        config.set(path, InventorySerializer.serializeContents(contents));
                    });

                    config.save(file);
                    log.info("Successfully saved {} stock containers.", snapshotMap.size());
                } catch (IOException e) {
                    log.error("Failed to save stock_containers.yml file!", e);
                }
            }
        });
    }

    public CompletableFuture<StockContainer> saveContainer(StockContainer container) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                try {
                    String path = "containers." + container.getOwner().toString();
                    config.set(path, InventorySerializer.serializeContents(container.getContentsForSave()));
                    config.save(file);
                } catch (IOException e) {
                    log.error("Failed to save a single container. (UUID: {})", container.getOwner(), e);
                }
                return container;
            }
        });
    }
}
