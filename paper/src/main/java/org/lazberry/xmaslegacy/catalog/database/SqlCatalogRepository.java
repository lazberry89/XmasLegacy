package org.lazberry.xmaslegacy.catalog.database;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.DatabaseProcessor;
import org.lazberry.xmaslegacy.catalog.CatalogProgress;
import org.lazberry.xmaslegacy.catalog.CatalogType;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InventorySerializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Catalog state as one BIGINT per (player, catalog type): bit n = slot n registered.
 * Saving ORs the bits inside MySQL, so two servers saving different slots at once both stick.
 */
@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class SqlCatalogRepository extends DatabaseProcessor {

	@Inject
	public SqlCatalogRepository(@ConfigValue(path = "web.database.url") String url,
	                            @ConfigValue(path = "web.database.user") String user,
	                            @ConfigValue(path = "web.database.password") String password) {
		super(url, user, password);
	}

	@Override
	protected void createTable() {
		String query = "CREATE TABLE IF NOT EXISTS user_catalog (" +
				"uuid VARCHAR(36) NOT NULL, " +
				"type VARCHAR(16) NOT NULL, " +
				"bits BIGINT NOT NULL DEFAULT 0, " +
				"reward_granted BOOLEAN NOT NULL DEFAULT FALSE, " +
				"PRIMARY KEY (uuid, type)" +
				")";
		String query2 = "CREATE TABLE IF NOT EXISTS catalog_sample (" +
				"type VARCHAR(36) NOT NULL PRIMARY KEY," +
				"sample MEDIUMTEXT NOT NULL" +
				");";
		try (Connection conn = getConnection();
		     PreparedStatement statement = conn.prepareStatement(query);
			 PreparedStatement statement2 = conn.prepareStatement(query2)) {
			statement.execute();
			statement2.execute();
			addRewardColumn(conn);
		} catch (SQLException e) {
			log.error("Failed to create catalog table.", e);
		}
	}

	/** For tables created before reward_granted existed; MySQL has no ADD COLUMN IF NOT EXISTS. */
	private static void addRewardColumn(Connection conn) throws SQLException {
		try (PreparedStatement statement = conn.prepareStatement(
				"ALTER TABLE user_catalog ADD COLUMN reward_granted BOOLEAN NOT NULL DEFAULT FALSE")) {
			statement.execute();
		} catch (SQLException e) {
			if (e.getErrorCode() != 1060) throw e; // 1060: the column is already there
		}
	}

	public CompletableFuture<Void> saveSampleCatalog(Map<CatalogType, ItemStack[]> sampleCatalog) {
		String query = "INSERT INTO catalog_sample (type, sample) VALUES (?, ?) " +
				"ON DUPLICATE KEY UPDATE sample = VALUES(sample)";

		return CompletableFuture.runAsync(() -> {
			try (Connection conn = getConnection();
			     PreparedStatement stmt = conn.prepareStatement(query)) {
				conn.setAutoCommit(false);

				for (var entry : sampleCatalog.entrySet()) {
					ItemStack[] value = entry.getValue();
					if (value == null || value.length == 0) {
						log.warn("catalog sample {} is empty. skipped.", entry.getKey());
						continue;
					}
					stmt.setString(1, entry.getKey().name());
					stmt.setString(2, InventorySerializer.serializeContents(value));
					stmt.addBatch();
				}

				stmt.executeBatch();
				conn.commit();
			} catch (SQLException e) {
				throw new CompletionException(e);
			}
		});
	}

	/** Fails exceptionally on DB error: an empty result would let an admin overwrite the samples with nothing. */
	public CompletableFuture<Map<CatalogType, String>> loadSampleCatalog() {
		String query = "SELECT type, sample FROM catalog_sample";
		var result = new ConcurrentHashMap<CatalogType, String>(CatalogType.values().length);
		return CompletableFuture.supplyAsync(() -> {
			try (Connection conn = getConnection();
			     PreparedStatement statement = conn.prepareStatement(query);
			     ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					String value = rs.getString("sample");
					if (value == null) continue;

					String typeStr = rs.getString("type");
					try {
						CatalogType type = CatalogType.valueOf(typeStr);
						result.put(type, value);
						log.info("Loaded sample inventory of type '{}'.", type.name());
					} catch (IllegalArgumentException e) {
						log.warn("Unknown catalog sample type '{}', skipped.", typeStr);
					}
				}
			} catch (SQLException e) {
				throw new CompletionException(e);
			}
			return result;
		});
	}

	/** Adds to what is stored, never clears. Fails exceptionally on DB error so the caller can retry. */
	public CompletableFuture<Void> save(UUID uuid, CatalogProgress progress) {
		String query = "INSERT INTO user_catalog (uuid, type, bits, reward_granted) VALUES (?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE bits = bits | VALUES(bits), reward_granted = reward_granted OR VALUES(reward_granted)";
		return CompletableFuture.runAsync(() -> {
			try (Connection conn = getConnection();
				PreparedStatement statement = conn.prepareStatement(query)) {
				conn.setAutoCommit(false);

				for (var type : CatalogType.values()) {
					long bits = progress.getBits(type);
					statement.setString(1, uuid.toString());
					statement.setString(2, type.name());
					statement.setLong(3, bits);
					statement.setBoolean(4, progress.isRewardGranted(type));
					statement.addBatch();
				}

				statement.executeBatch();
				conn.commit();
			} catch (SQLException e) {
				throw new CompletionException(e);
			}
		});
	}

	/** Loads every catalog type of the player. A player with no rows gets an empty progress. Completes with null on DB error. */
	public CompletableFuture<CatalogProgress> load(UUID uuid) {
		String query = "SELECT type, bits, reward_granted FROM user_catalog WHERE uuid = ?";
		return CompletableFuture.supplyAsync(() -> {
			try (Connection conn = getConnection();
			     PreparedStatement statement = conn.prepareStatement(query)) {

				statement.setString(1, uuid.toString());
				CatalogProgress progress = new CatalogProgress();
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						try {
							CatalogType type = CatalogType.valueOf(rs.getString("type"));
							progress.setBits(type, rs.getLong("bits"));
							progress.setRewardGranted(type, rs.getBoolean("reward_granted"));
						} catch (IllegalArgumentException e) {
							log.warn("Unknown catalog type '{}' for {}, skipped.", rs.getString("type"), uuid);
						}
					}
				}
				return progress;
			} catch (SQLException e) {
				// null, not empty: empty progress saved back would look like the player lost everything.
				log.error("Failed to load catalog of {}", uuid, e);
				return null;
			}
		});
	}
}
