package org.lazberry.xmaslegacy.bags.database;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.bags.Bag;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InventorySerializer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class BagSaveManager {
    private final SqlBagRepository bagRepository;

    @Inject
    public BagSaveManager(SqlBagRepository bagRepository) {
        this.bagRepository = bagRepository;
    }

    private String getBagKey(UUID uuid) {
        return "bag:owner:" + uuid + ":contents";
    }

    private void setJedis(Bag bag) {
        var uuid = bag.getOwner();
        var contents = bag.serializeContents();

        JedisService.setToJedis(getBagKey(uuid), contents);
    }

    private CompletableFuture<Bag> tryDatabase(UUID uuid) {
        return bagRepository.load(uuid)
                .thenApply(bag -> {
                    if (bag != null) {
                        setJedis(bag);
                        return bag;
                    }
                    return new Bag(uuid);
                })
                .exceptionally(ex -> {
                    log.error("Failed to load bag from database for user {}", uuid, ex);
                    return new Bag(uuid);
                });
    }

    public CompletableFuture<Bag> loadBag(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String contents = JedisService.getFromJedis(getBagKey(uuid));

            if (contents != null && !contents.isEmpty()) {
                try {
                    ItemStack[] itemContents = InventorySerializer.deserializeContents(contents);
                    return new Bag(uuid, itemContents);
                } catch (Exception e) {
                    log.error("Failed to load bag from cache, trying to load from database.", e);
                }
            }
            return null;
        }).thenCompose(bag -> {
            if (bag != null) {
                return CompletableFuture.completedFuture(bag);
            }
            return tryDatabase(uuid);
        });
    }

    public CompletableFuture<Void> saveBag(Bag bag) {
        return CompletableFuture.runAsync(() -> {
            setJedis(bag);
        }).thenCompose(v ->
                bagRepository.save(bag)
        ).thenApply(success -> {
            if (!success) log.error("Failed to persist bag for user {} to database during save chain.", bag.getOwner());
            return null;
        });
    }
}
