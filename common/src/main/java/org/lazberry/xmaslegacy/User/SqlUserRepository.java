package org.lazberry.xmaslegacy.User;

import org.lazberry.xmaslegacy.Roles.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class SqlUserRepository implements UserRepository {
	private static final Logger logger = LoggerFactory.getLogger(SqlUserRepository.class);

	private final String url = "jdbc:sqlite:plugins/XmasLegacy/database.db";
	private final String user = "root";
	private final String password = "your_password";

	public SqlUserRepository() {
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
				"isNewUser BOOLEAN, " +
				"wantsCookie BOOLEAN" +
				");";

		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.execute();
		} catch (SQLException e) {
			logger.error("Error occurred while Creating Table Task -> {}", e.getMessage(), e);
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
				Roles role = Roles.valueOf(rs.getString("role"));

				User loadedUser = new User(uuid, role, name);
				loadedUser.setDollars(rs.getInt("dollars"));
				loadedUser.setInquireCount(rs.getInt("inquireCount"));
				loadedUser.setPlayTime(rs.getInt("playTime"));
				loadedUser.setExp(rs.getDouble("Exp"));
				loadedUser.setNewUser(rs.getBoolean("isNewUser"));
				loadedUser.wantsCookie(rs.getBoolean("wantsCookie"));

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
		String sql = "INSERT OR REPLACE INTO users (uuid, name, role, dollars, inquireCount, playTime, Exp, isNewUser, wantsCookie) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, user.getUUID().toString());
			pstmt.setString(2, user.getName());
			pstmt.setString(3, user.getRole().getKor());
			pstmt.setInt(4, user.getDollars());
			pstmt.setInt(5, user.getInquireCount());
			pstmt.setInt(6, user.getPlayTime());
			pstmt.setDouble(7, user.getExp());
			pstmt.setBoolean(8, user.isNewUser());
			pstmt.setBoolean(9, user.ifWantsCookie());

			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("sql error while saving User -> {}", e.getMessage(), e);
		}
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
