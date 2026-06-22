package org.lazberry.xmaslegacy.User;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Roles.*;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.Tier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;

public enum SqlUserRepository implements UserRepository {
	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(SqlUserRepository.class);

	private final String url = "jdbc:sqlite:plugins/XmasLegacy/database.db";
	private final String user = "root";
	private final String password = "your_password"; //TODO need I/O process

	SqlUserRepository() {
		createTable();
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}

	private void createTable() {
		String sql = "CREATE TABLE IF NOT EXISTS users (" +
				"uuid VARCHAR(36) PRIMARY KEY, " +
				"name VARCHAR(16), " +
				"role VARCHAR(20), " +
				"dollars INT, " +
				"inquireCount INT, " +
				"playTime INT, " +
				"Exp DOUBLE, " +
				"roleExp DOUBLE," +
				"level INT," +
				"isNewUser BOOLEAN, " +
				"wantsCookie BOOLEAN, " +
				"tier VARCHAR(20), " +
				"mastery VARCHAR(20)," +
				"isImmuneToIcing BOOLEAN," +
				"icingState INT" +
				");";

		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			 stmt.execute();
		} catch (SQLException e) {
			logger.error("Error occurred while Creating Table Task -> {}", e.getMessage(), e);
		}
	}

	private @NotNull Role parseRole(String roleName) {
		if (roleName == null || roleName.isEmpty()) return BasicRoles.USER;

		try {
			return BasicRoles.valueOf(roleName);
		} catch (IllegalArgumentException e) {
			try {
				return SecondaryRoles.valueOf(roleName);
			} catch (IllegalArgumentException ex) {
				try {
					return ThirdRoles.valueOf(roleName);
				} catch (IllegalArgumentException exx) {
					try {
						return HiddenRoles.valueOf(roleName);
					} catch (IllegalArgumentException exc) {
						logger.warn("알 수 없는 직업명이 DB에서 발견되어 기본값으로 로드합니다: {}", roleName);
						return BasicRoles.USER;
					}
				}
			}
		}
	}

	@Override
	public User loadUser(UUID uuid) {
		String sql = "SELECT * FROM users WHERE uuid = ?";

		// try-with-resources: conn과 pstmt를 다 쓰면 자동으로 닫아줍니다 (메모리 누수 방지)
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, uuid.toString());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) { // 데이터가 존재한다면
				String name = rs.getString("name");
				Role role = parseRole(rs.getString("role"));

				User loadedUser = new User(uuid, role, name);
				loadedUser.setDollars(rs.getInt("dollars"));
				loadedUser.setInquireCount(rs.getInt("inquireCount"));
				loadedUser.setPlayTime(rs.getInt("playTime"));
				loadedUser.setExp(rs.getDouble("Exp"));
				loadedUser.setRoleExp(rs.getDouble("roleExp"));
				loadedUser.setLevel(rs.getInt("level"));
				loadedUser.setNewUser(rs.getBoolean("isNewUser"));
				loadedUser.wantsCookie(rs.getBoolean("wantsCookie"));

				Tier tier = Tier.valueOf(rs.getString("tier"));
				RoleMastery mastery = RoleMastery.valueOf(rs.getString("mastery"));
				loadedUser.setTier(tier);
				loadedUser.setMastery(mastery);
				loadedUser.setImmuneToIcing(rs.getBoolean("isImmuneToIcing"));
				loadedUser.setIcingState(rs.getInt("icingState"));

				return loadedUser;
			}
		} catch (SQLException e) {
			logger.error("Sql error while loading User '{}' -> {}", uuid, e.getMessage(), e);
		}
		return null;
	}
	//for mongoDB
	/*
	@Override
	public void saveUser(User user) {
		String sql = "INSERT INTO users (uuid, name, role, dollars, inquireCount, playTime, isNewUser, wantsCookie) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE " +
				"name=VALUES(name), role=VALUES(role), dollars=VALUES(dollars), " +
				"inquireCount=VALUES(inquireCount), playTime=VALUES(playTime), " +
				"isNewUser=VALUES(isNewUser), wantsCookie=VALUES(wantsCookie)";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// ? 자리에 값 채워넣기
			pstmt.setString(1, user.getUUID().toString());
			pstmt.setString(2, user.getName());
			pstmt.setString(3, user.getRole() != null ? user.getRole().name() : Roles.USER.name());
			pstmt.setInt(4, user.getDollars());
			pstmt.setInt(5, user.getInquireCount());
			pstmt.setInt(6, user.getPlayTime());
			pstmt.setBoolean(7, user.isNewUser());
			pstmt.setBoolean(8, user.ifWantsCookie());

			pstmt.executeUpdate(); // 실행!

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	*/
	@Override
	public void saveUser(User user) {
		// SQLite에서는 INSERT OR REPLACE INTO가 가장 간단합니다.
		String sql = "INSERT OR REPLACE INTO users (uuid, name, role, dollars, inquireCount, playTime, Exp, roleExp, level, isNewUser, wantsCookie, tier, mastery, isImmuneToIcing, icingState) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, user.getUUID().toString());
			pstmt.setString(2, user.getName());
			pstmt.setString(3, user.getRole().name());
			pstmt.setInt(4, user.getDollars());
			pstmt.setInt(5, user.getInquireCount());
			pstmt.setInt(6, user.getPlayTime());
			pstmt.setDouble(7, user.getExp());
			pstmt.setDouble(8, user.getRoleExp());
			pstmt.setInt(9, user.getLevel());
			pstmt.setBoolean(10, user.isNewUser());
			pstmt.setBoolean(11, user.ifWantsCookie());
			pstmt.setString(12, user.getTier().name());
			pstmt.setString(13, user.getMastery().name());
			pstmt.setBoolean(14, user.isImmuneToIcing());
			pstmt.setInt(15, user.getIcingState());

			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("sql error while saving User -> {}", e.getMessage(), e);
		}
	}

	@Override
	public int getRank(@NotNull UUID uuid) {
		String sql = "SELECT rank FROM ( " +
				"    SELECT uuid, RANK() OVER (ORDER BY Exp DESC) AS rank " +
				"    FROM users " +
				") WHERE uuid = ?";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, uuid.toString());

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("rank");
				}
			}
		} catch (SQLException e) {
			logger.error("Error while finding user Rank (UUID: {}) -> {}", uuid, e.getMessage());
		}
		return -1; // 유저를 찾지 못했거나 에러 발생 시
	}

	@Override
	public boolean exist(UUID uuid) {
		String sql = "SELECT 1 FROM users WHERE uuid = ? LIMIT 1";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, uuid.toString());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			logger.error("Sql error during exist -> {}", e.getMessage(), e);
			return false;
		}
	}
}
