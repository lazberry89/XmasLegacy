package org.lazberry.xmaslegacy.api;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Blocking;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.InitiatePriority;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Priority;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class ProductRecordManager implements Initiator {
    private final ProductRecordRepository repository;
    private final String key = "ungranted_products";
    private final String checkKey = "ungranted_uuids";

    @Inject
    public ProductRecordManager(ProductRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    @Priority(InitiatePriority.FAST)
    public void init() {
        warmUpSync();
    }

    @Blocking
    private void warmUpSync() {
        repository.getAllUngrantedProducts().whenComplete((list, ex) -> {
            if (ex != null) {
                log.error("Failed to warmup ungranted");
                return;
            }
            if (list.isEmpty()) {
                log.warn("Ungranted product doesn't exists. Skipping warmup.");
                return;
            }
            list.forEach(webProduct -> {
                JedisService.sadd(key, webProduct.toJson());
                JedisService.sadd(checkKey, webProduct.getReceiver().toString());
            });
        });
    }

    public void addProductAsPended(WebProduct product) {
        JedisService.sadd(key, product.toJson());
        JedisService.sadd(checkKey, product.getReceiver().toString());
        repository.saveProduct(product, false);
    }

    public boolean hasPendedProduct(UUID uuid) {
        return JedisService.sismember(checkKey, uuid.toString());
    }

    public CompletableFuture<Void> handlePendedProduct(UUID uuid) {
        return repository.getUngrantedProducts(uuid).handle((list, ex) -> {
            if (ex != null) {
                log.error("Failed to query database for {}", uuid);
                return null;
            }
            if (list.isEmpty()) {
                log.warn("No pended web products.");
                return null;
            }
            list.forEach(webProduct -> {
                repository.markAsGranted(webProduct).exceptionally(exc -> {
                    log.error("Internal error occurred.", exc);
                    return null;
                });
                JedisService.srem(key, webProduct.toJson());
                JedisService.srem(checkKey, uuid.toString());
            });
            return null;
        });
    }

    @Override
    public void close() {
        JedisService.removeKey(key);
        JedisService.removeKey(checkKey);
    }
}
