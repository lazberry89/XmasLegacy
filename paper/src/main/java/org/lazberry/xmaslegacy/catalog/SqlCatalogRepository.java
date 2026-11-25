package org.lazberry.xmaslegacy.catalog;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.DatabaseProcessor;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Catalog state as one BIGINT per (player, catalog type): bit n = slot n registered.
 * Registering ORs the bit inside MySQL, so two servers registering different slots at once both stick.
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
				"PRIMARY KEY (uuid, type)" +
				")";
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.execute();
		} catch (SQLException e) {
			log.error("Failed to create user_catalog table -> {}", e.getMessage(), e);
		}
	}

	/** Loads every catalog type of the player. A player with no rows gets an empty progress. Completes with null on DB error. */
	public CompletableFuture<CatalogProgress> load(UUID uuid) {
		return CompletableFuture.supplyAsync(() -> {
			String query = "SELECT type, bits FROM user_catalog WHERE uuid = ?";
			try (Connection conn = getConnection();
			     PreparedStatement statement = conn.prepareStatement(query)) {

				statement.setString(1, uuid.toString());
				CatalogProgress progress = new CatalogProgress();
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						try {
							CatalogType type = CatalogType.valueOf(rs.getString("type"));
							progress.setBits(type, rs.getLong("bits"));
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

	/** Sets one slot a bit in the database. Completes with false on DB error. */
	public CompletableFuture<Boolean> register(UUID uuid, CatalogType type, int slot) {
		CatalogProgress.checkSlot(slot);
		return CompletableFuture.supplyAsync(() -> {
			String query = "INSERT INTO user_catalog (uuid, type, bits) VALUES (?, ?, ?) " +
					"ON DUPLICATE KEY UPDATE bits = bits | VALUES(bits)";
			try (Connection conn = getConnection();
			     PreparedStatement statement = conn.prepareStatement(query)) {
				statement.setString(1, uuid.toString());
				statement.setString(2, type.name());
				statement.setLong(3, 1L << slot);
				statement.executeUpdate();
				return true;
			} catch (SQLException e) {
				log.error("Failed to register catalog slot {}:{} for {}", type, slot, uuid, e);
				return false;
			}
		});
	}
}
