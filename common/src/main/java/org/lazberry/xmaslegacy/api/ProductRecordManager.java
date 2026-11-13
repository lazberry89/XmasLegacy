package org.lazberry.xmaslegacy.api;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Main manager of web purchases. Every purchase is identified by its purchase number
 * ({@link WebProduct#getId()}), so buying the same product twice makes two separate records.
 * <p>
 * Ungranted purchases live in Redis, which the proxy and every paper server share:
 * <ul>
 * <li>hash {@code ungranted_products}: purchase number -> purchase (json)</li>
 * <li>set {@code ungranted_products:<uuid>}: purchase numbers waiting for that user</li>
 * </ul>
 * The database is written on every change (insert on purchase, update on grant), so it is always
 * the source of truth and nothing has to be uploaded from Redis on shutdown. Redis is only a
 * shared cache that is rebuilt from the database on startup.
 */
@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class ProductRecordManager implements Initiator {
    private static final String KEY = "ungranted_products";

    private final ProductRecordRepository repository;

    @Inject
    public ProductRecordManager(ProductRecordRepository repository) {
        this.repository = repository;
    }

    private static String userKey(UUID uuid) {
        return KEY + ":" + uuid;
    }

    /**
     * Warm-up: database -> Redis. Default (AVERAGE) priority on purpose, so it always runs
     * after JedisService (FAST) has connected.
     */
    @Override
    public void init() {
        repository.getAllUngrantedProducts().thenAccept(list -> {
            list.forEach(this::remember);
            log.info("Warmed up {} ungranted product(s) into Redis.", list.size());
        }).exceptionally(ex -> {
            log.error("Failed to warmup ungranted products.", ex);
            return null;
        });
    }

    private void remember(WebProduct product) {
        String id = String.valueOf(product.getId());
        JedisService.hset(KEY, id, product.toJson());
        JedisService.sadd(userKey(product.getReceiver()), id);
    }

    private void forget(WebProduct product) {
        String id = String.valueOf(product.getId());
        JedisService.hdel(KEY, id);
        JedisService.srem(userKey(product.getReceiver()), id);
    }

    /**
     * Records a new purchase as ungranted: database first (it issues the purchase number), then Redis.
     * @return the stored purchase. Fails exceptionally if either write fails; a purchase that reached
     * the database but not Redis is picked up by the next warm-up.
     */
    public CompletableFuture<WebProduct> addProductAsPended(UUID receiver, ProductType type) {
        return repository.saveProduct(receiver, type).thenApply(id -> {
            WebProduct product = new WebProduct(id, receiver, type);
            remember(product);
            return product;
        });
    }

    public boolean hasPendedProduct(UUID uuid) {
        return !JedisService.smembers(userKey(uuid)).isEmpty();
    }

    /** @return purchase numbers still waiting to be granted to this user. */
    public Set<Long> getPendedIds(UUID uuid) {
        return JedisService.smembers(userKey(uuid)).stream().map(Long::valueOf).collect(Collectors.toSet());
    }

    /**
     * Looks the purchase up in Redis, falling back to the database (e.g. after Redis lost its data).
     * @return the ungranted purchase, or null if it doesn't exist or is already granted.
     */
    public CompletableFuture<WebProduct> find(long id) {
        return CompletableFuture.supplyAsync(() -> JedisService.hget(KEY, String.valueOf(id)))
                .thenCompose(json -> {
                    if (json != null) return CompletableFuture.completedFuture(WebProduct.fromJson(json));
                    return repository.getUngrantedProduct(id).thenApply(product -> {
                        if (product != null) remember(product);
                        return product;
                    });
                });
    }

    /**
     * Claims the purchase as granted in the database, then removes it from Redis.
     * @return true only for the single caller that won the claim. Give the reward only when true.
     */
    public CompletableFuture<Boolean> markAsGranted(WebProduct product) {
        return repository.setGranted(product.getId(), true).thenApply(claimed -> {
            // Remove even when the claim was lost: the database says it is already granted,
            // so the Redis entry was stale.
            forget(product);
            if (claimed) product.setGranted(true);
            return claimed;
        });
    }

    /** Puts a claimed purchase back to ungranted, for when the reward could not be given after all. */
    public CompletableFuture<Void> revertGrant(WebProduct product) {
        return repository.setGranted(product.getId(), false).thenAccept(reverted -> {
            if (!reverted) return;
            product.setGranted(false);
            remember(product);
        });
    }

    /** Redis is shared with the other servers and the database is already up to date: nothing to flush or wipe. */
    @Override
    public void close() {
    }
}
