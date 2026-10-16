package org.lazberry.xmaslegacy.bags.database;

import org.lazberry.xmaslegacy.bags.Bag;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BagRepository {
    CompletableFuture<Boolean> save(Bag bag);
    CompletableFuture<Bag> load(UUID uuid);
}
