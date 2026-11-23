package org.lazberry.xmaslegacy.prefix;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.DatabaseProcessor;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.settings.ServerPrefix;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class SqlPrefixRepository extends DatabaseProcessor {

	@Inject
	public SqlPrefixRepository(@ConfigValue(path = "web.database.url") String url,
	                              @ConfigValue(path = "web.database.user") String user,
	                              @ConfigValue(path = "web.database.password") String password) {
		super(url, user, password);
	}

	@Override
	protected void createTable() {
		String query = "CREATE TABLE IF NOT EXISTS unique_prefix (" +
				"prefix VARCHAR(64) PRIMARY KEY, " +
				"uuid VARCHAR(36) NOT NULL, " +
				"acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
				");";
		try (Connection conn = getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.execute();
			}
		} catch (SQLException e) {
			log.error("Error occurred while Creating Table Task -> {}", e.getMessage(), e);
		}
	}

	public CompletableFuture<Boolean> isPrefixAcquired(ServerPrefix prefix) {
		return CompletableFuture.supplyAsync(() -> {
			String query = "SELECT 1 FROM unique_prefix WHERE prefix = ? LIMIT 1";
			try (Connection conn = getConnection();
				PreparedStatement statement = conn.prepareStatement(query)) {
				statement.setString(1, prefix.name());

				return statement.executeQuery().next();
			} catch (SQLException e) {
				log.error("Failed to send query to database. Check logs -> ", e);
				return true; // Returns true to prevent from unchecked prefix being given to player.
			}
		});
	}

	public CompletableFuture<Boolean> grantPrefix(UUID uuid, ServerPrefix prefix) {
		return CompletableFuture.supplyAsync(() -> {
			String query = "INSERT IGNORE INTO unique_prefix (prefix, uuid) VALUES (?, ?)";

			try (Connection conn = getConnection();
			     PreparedStatement statement = conn.prepareStatement(query)) {

				statement.setString(1, prefix.name());
				statement.setString(2, uuid.toString());

				return statement.executeUpdate() == 1; // 0 = someone already owns it
			} catch (SQLException e) {
				log.error("Failed to grant prefix '{}' to '{}'", prefix.name(), uuid, e);
				return false;
			}
		});
	}

	public CompletableFuture<PrefixStatus> getPrefixStatus(ServerPrefix prefix) {
		return CompletableFuture.supplyAsync(() -> {
			String query = "SELECT * FROM unique_prefix WHERE prefix = ? LIMIT 1";

			try (Connection conn = getConnection();
				PreparedStatement statement = conn.prepareStatement(query)) {
				statement.setString(1, prefix.name());

				ResultSet result = statement.executeQuery();
				if (result.next()) {
					try {
						ServerPrefix parsed = ServerPrefix.parse(
								result.getString("prefix"), null);
						UUID owner = UUID.fromString(result.getString("uuid"));
						Timestamp givenAt = result.getTimestamp("acquired_at");

						return new PrefixStatus(parsed, owner, givenAt);
					} catch (IllegalArgumentException e) {
						log.error("Failed to parse status from database. Check logs -> ", e);
						return null;
					}
				} else {
					return null;
				}
			} catch (SQLException e) {
				log.error("Failed to send query to database. Check logs -> ", e);
				return null;
			}
		});
	}
}
