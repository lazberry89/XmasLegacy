package org.lazberry.xmaslegacy.catalog.database;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.catalog.CatalogProgress;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Redis holds the live catalog of every player, shared by all servers. Changed players are listed in
 * {@link #DIRTY} and written to the database by {@link #flush()}, every few minutes and on shutdown.
 * Both directions only ever add bits, so neither a stale Redis snapshot nor a stale row can erase progress.
 */
@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class CatalogSaveManager implements Initiator {
	private static final String DIRTY = "user_catalog:dirty";

	private final SqlCatalogRepository repository;

	@Inject
	public CatalogSaveManager(SqlCatalogRepository repository) {
		this.repository = repository;
	}

	private static String key(Object uuid) {
		return "user_catalog:" + uuid;
	}

	/** Completes with null when the catalog could not be read; never treat that as an empty catalog. */
	public CompletableFuture<CatalogProgress> loadWhenJoin(UUID uuid) {
		return CompletableFuture.supplyAsync(() -> readRedis(uuid))
				.thenCombine(repository.load(uuid), (cached, stored) -> reconcile(uuid, cached, stored))
				.exceptionally(ex -> {
					log.error("Failed to load catalog of {}", uuid, ex);
					return null;
				});
	}

	private @Nullable CatalogProgress reconcile(UUID uuid, @Nullable CatalogProgress cached, @Nullable CatalogProgress stored) {
		if (stored == null) return cached;
		if (cached == null) {
			JedisService.setToJedis(key(uuid), stored.toJson());
			return stored;
		}
		CatalogProgress merged = cached.copy();
		merged.merge(stored);
		if (!merged.isSameAs(cached)) JedisService.setToJedis(key(uuid), merged.toJson());
		if (!merged.isSameAs(stored)) JedisService.sadd(DIRTY, uuid.toString());
		return merged;
	}

	/** null when missing or unreadable, so a broken value falls back to the database. Throws if Redis is down. */
	private @Nullable CatalogProgress readRedis(Object uuid) {
		String json = JedisService.getFromJedis(key(uuid));
		if (json == null) return null;
		try {
			return CatalogProgress.fromJson(json);
		} catch (RuntimeException e) {
			log.error("Broken catalog JSON in Redis for {}", uuid, e);
			return null;
		}
	}

	/** Writes to Redis only; the database follows on the next flush. Throws if Redis is down. */
	public void save(@NotNull UUID uuid, @NotNull CatalogProgress progress) {
		JedisService.setToJedis(key(uuid), progress.toJson());
		JedisService.sadd(DIRTY, uuid.toString()); // after the value, or a flush in between would miss it
	}

	/** synchronized: close() waits for a periodic flush that is still running. */
	@Blocking
	public synchronized void flush() {
		try {
			for (String uuid : JedisService.smembers(DIRTY)) flush(uuid);
		} catch (RuntimeException e) {
			log.error("Catalog flush stopped early; the remaining players stay marked for the next one.", e);
		}
	}

	private void flush(String uuid) {
		// Unmark before reading: a registration made meanwhile marks the player again.
		JedisService.srem(DIRTY, uuid);
		try {
			CatalogProgress progress = readRedis(uuid);
			if (progress != null) repository.save(UUID.fromString(uuid), progress).join();
		} catch (RuntimeException e) {
			log.error("Failed to flush catalog of {}", uuid, e);
			JedisService.sadd(DIRTY, uuid);
		}
	}

	@Override
	public void init() {}

	/** Runs before JedisService.close(), which has the DELAYED close priority. */
	@Override
	public void close() {
		flush();
	}
}
