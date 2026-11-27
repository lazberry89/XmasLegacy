package org.lazberry.xmaslegacy.catalog;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InventorySerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: wire SqlCatalogRepository + CatalogProgress — load into a per-player cache on join, drop it on quit,
//  and when a slot is filled call repository.register only if progress.register(type, slot) returned true.
//  The catalog GUI (catalog/gui) and per-type slot→item lists are not written yet.

@Components.Exclude(type = ServerType.LOBBY)
public class CatalogManager implements Initiator {
    private final Map<CatalogType, ItemStack[]> sampleInv = new HashMap<>(CatalogType.values().length);
    private final SqlCatalogRepository repository;

    @Inject
    public CatalogManager(SqlCatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void init() {
        sampleInv.clear();
        sampleInv.putAll(parseSqlOutput());
    }

    @Blocking
    private Map<CatalogType, ItemStack[]> parseSqlOutput() {
        Map<CatalogType, String> output = repository.loadSampleCatalog().join();
        return output.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                return InventorySerializer.deserializeContents(entry.getValue());
                            } catch (Exception e) {
                                return new ItemStack[]{};
                            }
                        }
                ));
    }

    @Override
    public void close() {
    }
}
