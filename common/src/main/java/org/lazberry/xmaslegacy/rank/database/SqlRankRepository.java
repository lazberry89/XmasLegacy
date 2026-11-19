package org.lazberry.xmaslegacy.rank.database;

import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.rank.RankType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Components.Exclude(type = ServerType.LOBBY)
public class SqlRankRepository implements RankRepository {
	private static final Logger log = LoggerFactory.getLogger(SqlRankRepository.class);

	private final String url;
	private final String user;
	private final String password;

	@Inject
	public SqlRankRepository(@ConfigValue(path = "web.database.url") String url,
	                         @ConfigValue(path = "web.database.user") String user,
	                         @ConfigValue(path = "web.database.password") String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}

	@Override
	public CompletableFuture<Map<UUID, Double>> loadRankData(RankType rankType) {
		return CompletableFuture.supplyAsync(() -> {
			synchronized (this) {
				Map<UUID, Double> rankData = new HashMap<>();
				String query = "SELECT uuid, dollars, Exp, roleExp, playTime FROM users";

				try (PreparedStatement preparedStatement = getConnection().prepareStatement(query);
				     ResultSet resultSet = preparedStatement.executeQuery()) {

					while (resultSet.next()) {
						UUID uuid = UUID.fromString(resultSet.getString("uuid"));
						double value = switch (rankType) {
							case DOLLAR -> resultSet.getInt("dollars");
							case EXP -> resultSet.getInt("Exp");
							case ROLE_EXP -> resultSet.getInt("roleExp");
							case PLAYTIME -> resultSet.getInt("playTime");
						};
						rankData.put(uuid, value);
					}
					return rankData;
				} catch (SQLException e) {
					log.error("Error while loading rank data.", e);
					return null;
				} catch (Exception e) {
					log.error("Error occurred white parsing values from query.", e);
					return null;
				}
			}
		});
	}
}
