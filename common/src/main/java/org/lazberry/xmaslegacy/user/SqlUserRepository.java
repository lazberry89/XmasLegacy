package org.lazberry.xmaslegacy.user;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.roles.Role;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.settings.Tier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

import static org.lazberry.xmaslegacy.user.User.GSON;

@Data
@Registry.Exclude(type = ServerType.LOBBY)
public class SqlUserRepository implements UserRepository {
	private final String url = "jdbc:mysql://localhost:3306/xmas_legacy?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
	private final String user = "husksync";
	private final String password = "husksync";

	private static final @NotNull Logger log = LoggerFactory.getLogger(SqlUserRepository.class);

	@Inject
	public SqlUserRepository() {
		createTable();
	}

	private @NotNull Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}

	private void createTable() {
		String sql = "CREATE TABLE IF NOT EXISTS users (" +
				"uuid VARCHAR(36) PRIMARY KEY, " +
				"name VARCHAR(16), " +
				"role VARCHAR(20), " +
				"dollars BIGINT, " +
				"inquireCount INT, " +
				"playTime BIGINT, " +
				"Exp INT, " +
				"roleExp INT," +
				"sinPoints INT," +
				"level INT," +
				"isNewUser BOOLEAN, " +
				"wantsCookie BOOLEAN, " +
				"tier VARCHAR(20), " +
				"mastery VARCHAR(20), " +
				"isImmuneToIcing BOOLEAN, " +
				"icingState INT, " +
				"showBoard BOOLEAN, " +
				"isMobile BOOLEAN, " +
				"availablePrefix TEXT, " +
				"equipPrefix VARCHAR(50)" +
				");";

		try (Connection conn = getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.execute();
			}
			migrateColumns(conn);
		} catch (SQLException e) {
			log.error("Error occurred while Creating Table Task -> {}", e.getMessage(), e);
		}
	}

	private void migrateColumns(@NotNull Connection conn) {
		Set<String> existingColumns = new HashSet<>();
		try {
			DatabaseMetaData meta = conn.getMetaData();
			try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, "users", "%")) {
				while (rs.next()) {
					existingColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
				}
			}
		} catch (SQLException e) {
			log.warn("Failed to retrieve columns via DatabaseMetaData: {}", e.getMessage());
		}

		if (existingColumns.isEmpty()) {
			try (Statement stmt = conn.createStatement();
			     ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM users")) {
				while (rs.next()) {
					existingColumns.add(rs.getString("Field").toLowerCase());
				}
			} catch (SQLException e) {
				log.warn("Failed to retrieve columns via SHOW COLUMNS: {}", e.getMessage());
			}
		}

		Map<String, String> requiredColumns = new LinkedHashMap<>();
		requiredColumns.put("name", "VARCHAR(16)");
		requiredColumns.put("role", "VARCHAR(20)");
		requiredColumns.put("dollars", "BIGINT DEFAULT 0");
		requiredColumns.put("inquireCount", "INT DEFAULT 0");
		requiredColumns.put("playTime", "BIGINT DEFAULT 0");
		requiredColumns.put("Exp", "INT DEFAULT 0");
		requiredColumns.put("roleExp", "INT DEFAULT 0");
		requiredColumns.put("sinPoints", "INT DEFAULT 0");
		requiredColumns.put("level", "INT DEFAULT 0");
		requiredColumns.put("isNewUser", "BOOLEAN DEFAULT FALSE");
		requiredColumns.put("wantsCookie", "BOOLEAN DEFAULT TRUE");
		requiredColumns.put("tier", "VARCHAR(20) DEFAULT 'VISITOR'");
		requiredColumns.put("mastery", "VARCHAR(20) DEFAULT 'BEGINNER'");
		requiredColumns.put("isImmuneToIcing", "BOOLEAN DEFAULT FALSE");
		requiredColumns.put("icingState", "INT DEFAULT 100");
		requiredColumns.put("showBoard", "BOOLEAN DEFAULT TRUE");
		requiredColumns.put("isMobile", "BOOLEAN DEFAULT FALSE");
		requiredColumns.put("availablePrefix", "TEXT");
		requiredColumns.put("equipPrefix", "VARCHAR(50)");

		for (Map.Entry<String, String> entry : requiredColumns.entrySet()) {
			String column = entry.getKey();
			String definition = entry.getValue();
			if (!existingColumns.contains(column.toLowerCase())) {
				String alterSql = "ALTER TABLE users ADD COLUMN " + column + " " + definition;
				try (Statement alterStmt = conn.createStatement()) {
					alterStmt.executeUpdate(alterSql);
					log.info("Successfully added missing column '{}' to table 'users'.", column);
				} catch (SQLException e) {
					// 1060: Duplicate column name (if column was already present)
					if (e.getErrorCode() != 1060 && (e.getMessage() == null || !e.getMessage().contains("Duplicate column"))) {
						log.error("Failed to add missing column '{}' to table 'users': {}", column, e.getMessage(), e);
					}
				}
			}
		}
	}

	@Override
	public @Nullable User loadUser(@NotNull UUID uuid) {
		String sql = "SELECT * FROM users WHERE uuid = ?";

		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, uuid.toString());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) { // 데이터가 존재한다면
				String name = getStringSafe(rs, "name", "Unknown");
				Role role = Role.parseRole(getStringSafe(rs, "role", ServerRoles.USER.name()), ServerRoles.USER);

				User loadedUser = new User(uuid, role, name);
				loadedUser.setDollars(getLongSafe(rs, "dollars", 0L));
				loadedUser.setInquireCount(getIntSafe(rs, "inquireCount", 0));
				loadedUser.setPlayTime(getLongSafe(rs, "playTime", 0L));
				loadedUser.setExp(getIntSafe(rs, "Exp", 0));
				loadedUser.setRoleExp(getIntSafe(rs, "roleExp", 0));
				loadedUser.setSinPoints(getIntSafe(rs, "sinPoints", 0));
				loadedUser.setLevel(getIntSafe(rs, "level", 0));
				loadedUser.setNewUser(getBooleanSafe(rs, "isNewUser", false));
				loadedUser.wantsCookie(getBooleanSafe(rs, "wantsCookie", true));

				String tierStr = getStringSafe(rs, "tier", Tier.VISITOR.name());
				Tier tier = Tier.VISITOR;
				try {
					tier = Tier.valueOf(tierStr);
				} catch (IllegalArgumentException ignored) {}

				String masteryStr = getStringSafe(rs, "mastery", RoleMastery.BEGINNER.name());
				RoleMastery mastery = RoleMastery.BEGINNER;
				try {
					mastery = RoleMastery.valueOf(masteryStr);
				} catch (IllegalArgumentException ignored) {}

				loadedUser.setTier(tier);
				loadedUser.setMastery(mastery);
				loadedUser.setImmuneToIcing(getBooleanSafe(rs, "isImmuneToIcing", false));
				loadedUser.setIcingState(getIntSafe(rs, "icingState", 100));
				loadedUser.setShowBoard(getBooleanSafe(rs, "showBoard", true));
				loadedUser.setMobile(getBooleanSafe(rs, "isMobile", false));

				String prefixJson = getStringSafe(rs, "availablePrefix", null);
				if (prefixJson != null && !prefixJson.isEmpty()) {
					try {
						List<ServerPrefix> prefixes = GSON.fromJson(
								prefixJson,
								new com.google.gson.reflect.TypeToken<List<ServerPrefix>>(){}.getType()
						);
						loadedUser.getAvailablePrefix().clear();
						loadedUser.getAvailablePrefix().addAll(prefixes);
					} catch (Exception e) {
						log.warn("Failed to parse availablePrefix for user {}: {}", uuid, e.getMessage());
					}
				}
				String equipName = getStringSafe(rs, "equipPrefix", null);
				if (equipName != null && !equipName.isEmpty()) {
					ServerPrefix equip = ServerPrefix.parse(equipName, Tier.VISITOR);
					loadedUser.setEquipPrefix(equip);
				} else {
					loadedUser.setEquipPrefix(null);
				}

				return loadedUser;
			}
		} catch (SQLException e) {
			log.error("Sql error while loading User '{}' -> {}", uuid, e.getMessage(), e);
		}
		return null;
	}

	private boolean getBooleanSafe(@NotNull ResultSet rs, @NotNull String column, boolean def) {
		try {
			return rs.getBoolean(column);
		} catch (SQLException e) {
			return def;
		}
	}

	private int getIntSafe(@NotNull ResultSet rs, @NotNull String column, int def) {
		try {
			return rs.getInt(column);
		} catch (SQLException e) {
			return def;
		}
	}

	private long getLongSafe(@NotNull ResultSet rs, @NotNull String column, long def) {
		try {
			return rs.getLong(column);
		} catch (SQLException e) {
			return def;
		}
	}

	private @Nullable String getStringSafe(@NotNull ResultSet rs, @NotNull String column, @Nullable String def) {
		try {
			String val = rs.getString(column);
			return val != null ? val : def;
		} catch (SQLException e) {
			return def;
		}
	}

	@Override
	public void saveUser(@NotNull User user) {
		String sql = "REPLACE INTO users (uuid, name, role, dollars, inquireCount, playTime, Exp, roleExp, sinPoints, level, isNewUser, wantsCookie, tier, mastery, isImmuneToIcing, icingState, showBoard, isMobile, availablePrefix, equipPrefix) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, user.getUniqueId().toString());
			pstmt.setString(2, user.getName());
			pstmt.setString(3, user.getRole() != null ? user.getRole().name() : ServerRoles.USER.name());
			pstmt.setLong(4, user.getDollars());
			pstmt.setInt(5, user.getInquireCount());
			pstmt.setLong(6, user.getPlayTime());
			pstmt.setInt(7, user.getExp());
			pstmt.setInt(8, user.getRoleExp());
			pstmt.setInt(9, user.getSinPoints());
			pstmt.setInt(10, user.getLevel());
			pstmt.setBoolean(11, user.isNewUser());
			pstmt.setBoolean(12, user.ifWantsCookie());
			pstmt.setString(13, user.getTier() != null ? user.getTier().name() : Tier.VISITOR.name());
			pstmt.setString(14, user.getMastery() != null ? user.getMastery().name() : RoleMastery.BEGINNER.name());
			pstmt.setBoolean(15, user.isImmuneToIcing());
			pstmt.setInt(16, user.getIcingState());
			pstmt.setBoolean(17, user.isShowBoard());
			pstmt.setBoolean(18, user.isMobile());

			String prefixJson = GSON.toJson(user.getAvailablePrefix());
			pstmt.setString(19, prefixJson);
			if (user.getEquipPrefix() != null) {
				pstmt.setString(20, user.getEquipPrefix().name());
			} else {
				pstmt.setNull(20, java.sql.Types.VARCHAR);
			}

			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error("sql error while saving User -> {}", e.getMessage(), e);
			throw new RuntimeException("sql error while saving User", e);
		}
	}

	@Override
	public int getRank(@NotNull UUID uuid) {
		String sql = "SELECT rank FROM ( " +
				"    SELECT uuid, RANK() OVER (ORDER BY Exp DESC) AS rank " +
				"    FROM users " +
				") AS temp WHERE uuid = ?";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, uuid.toString());

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("rank");
				}
			}
		} catch (SQLException e) {
			log.error("Error while finding user Rank (UUID: {}) -> {}", uuid, e.getMessage());
		}
		return -1;
	}

	@Override
	public boolean exist(@NotNull UUID uuid) {
		String sql = "SELECT 1 FROM users WHERE uuid = ? LIMIT 1";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, uuid.toString());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			log.error("Sql error during exist -> {}", e.getMessage(), e);
			return false;
		}
	}
}
