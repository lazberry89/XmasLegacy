package org.lazberry.xmaslegacy.user;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.roles.Role;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.Tier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;

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
				"dollars INT, " +
				"inquireCount INT, " +
				"playTime INT, " +
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
				"showBoard BOOLEAN" +
				");";

		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			 stmt.execute();
		} catch (SQLException e) {
			log.error("Error occurred while Creating Table Task -> {}", e.getMessage(), e);
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
				String name = rs.getString("name");
				Role role = Role.parseRole(rs.getString("role"), ServerRoles.USER);

				User loadedUser = new User(uuid, role, name);
				loadedUser.setDollars(rs.getInt("dollars"));
				loadedUser.setInquireCount(rs.getInt("inquireCount"));
				loadedUser.setPlayTime(rs.getInt("playTime"));
				loadedUser.setExp(rs.getInt("Exp"));
				loadedUser.setRoleExp(rs.getInt("roleExp"));
				loadedUser.setSinPoints(rs.getInt("sinPoints"));
				loadedUser.setLevel(rs.getInt("level"));
				loadedUser.setNewUser(rs.getBoolean("isNewUser"));
				loadedUser.wantsCookie(rs.getBoolean("wantsCookie"));

				Tier tier = Tier.valueOf(rs.getString("tier"));
				RoleMastery mastery = RoleMastery.valueOf(rs.getString("mastery"));
				loadedUser.setTier(tier);
				loadedUser.setMastery(mastery);
				loadedUser.setImmuneToIcing(rs.getBoolean("isImmuneToIcing"));
				loadedUser.setIcingState(rs.getInt("icingState"));
				loadedUser.setShowBoard(rs.getBoolean("showBoard"));

				return loadedUser;
			}
		} catch (SQLException e) {
			log.error("Sql error while loading User '{}' -> {}", uuid, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public void saveUser(@NotNull User user) {
		String sql = "REPLACE INTO users (uuid, name, role, dollars, inquireCount, playTime, Exp, roleExp, sinPoints, level, isNewUser, wantsCookie, tier, mastery, isImmuneToIcing, icingState, showBoard) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, user.getUniqueId().toString());
			pstmt.setString(2, user.getName());
			pstmt.setString(3, user.getRole().name());
			pstmt.setInt(4, user.getDollars());
			pstmt.setInt(5, user.getInquireCount());
			pstmt.setInt(6, user.getPlayTime());
			pstmt.setInt(7, user.getExp());
			pstmt.setInt(8, user.getRoleExp());
			pstmt.setInt(9, user.getSinPoints());
			pstmt.setInt(10, user.getLevel());
			pstmt.setBoolean(11, user.isNewUser());
			pstmt.setBoolean(12, user.ifWantsCookie());
			pstmt.setString(13, user.getTier().name());
			pstmt.setString(14, user.getMastery().name());
			pstmt.setBoolean(15, user.isImmuneToIcing());
			pstmt.setInt(16, user.getIcingState());
			pstmt.setBoolean(17, user.isShowBoard());

			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error("sql error while saving User -> {}", e.getMessage(), e);
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
