package org.lazberry.xmaslegacy.bags.database;

import org.lazberry.xmaslegacy.utils.ServerUtils;
import org.lazberry.xmaslegacy.user.UserSaveManager;
import org.lazberry.xmaslegacy.XmasLegacy;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.bags.Bag;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InventorySerializer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class BagSaveManager implements Initiator {
    private final SqlBagRepository bagRepository;
    private final BagManager bagManager;
    private final UserSaveManager userSaveManager;

    @Inject
    public BagSaveManager(SqlBagRepository bagRepository, BagManager bagManager, UserSaveManager userSaveManager) {
        this.userSaveManager = userSaveManager;
        this.bagRepository = bagRepository;
        this.bagManager = bagManager;
    }

    @Override
    public void init() {
        loadBagsOnLoad();
    }

    @Blocking
    private void loadBagsOnLoad() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            var uuid = player.getUniqueId();
            try {
                Bag bag = loadBag(uuid).join();
                if (bag != null) {
                    bagManager.setUserBag(uuid, bag);
                }
            } catch (Exception e) {
                log.error("Failed to load bag for online player {} during reload", player.getName(), e);
            }
        });
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
                    // A failed read stays a failure: an empty bag would later be saved over the real one.
                    });
    }

    /** Waits for the previous server to finish saving first. */
    public CompletableFuture<Bag> loadBagWhenJoin(UUID uuid) {
        String server = ServerUtils.getServerType(XmasLegacy.getInstance()).name();
        return CompletableFuture.runAsync(() -> userSaveManager.awaitPreviousServer(uuid, server), Thread::startVirtualThread)
                .thenCompose(v -> loadBag(uuid));
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
        // Redis right away, on the calling thread: the next server may read it at any moment.
        try {
            setJedis(bag);
        } catch (RuntimeException e) {
            log.error("Failed to cache bag of {} in Redis.", bag.getOwner(), e);
        }
        return bagRepository.save(bag).thenApply(success -> {
            if (!success) log.error("Failed to persist bag for user {} to database during save chain.", bag.getOwner());
            return null;
        });
    }

    public void saveOnlinePlayer() {
        var futures = Bukkit.getOnlinePlayers().stream()
                .filter(Player::isValid)
                .map(Player::getUniqueId)
                .map(bagManager::getBag)
                .filter(Objects::nonNull)
                .map(this::saveBag)
                .toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(futures).join();
            log.info("Successfully saved all online players' bags during shutdown.");
        } catch (Exception e) {
            log.error("An error occurred while saving bags during server shutdown.", e);
        }
    }

    @Override
    public void close() {
        saveOnlinePlayer();
        bagManager.clear();
    }
}
