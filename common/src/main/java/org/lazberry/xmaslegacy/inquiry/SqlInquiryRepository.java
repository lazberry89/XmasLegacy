package org.lazberry.xmaslegacy.inquiry;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Registry.Include(type = ServerType.GLOBAL)
public class SqlInquiryRepository {
	private static final Logger logger = LoggerFactory.getLogger(SqlInquiryRepository.class);

	private final String url = "jdbc:mysql://localhost:3306/xmas_legacy?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
	private final String user = "husksync";
	private final String password = "husksync";

	public SqlInquiryRepository() {
		createNewTable();
	}

	private void createNewTable() {
		String sql = """
            CREATE TABLE IF NOT EXISTS inquiry_logs (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                uuid VARCHAR(36) NOT NULL,
                player_name VARCHAR(16) NOT NULL,
                message TEXT NOT NULL,
                answer TEXT DEFAULT NULL,
                status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

		try (Connection conn = DriverManager.getConnection(url, user, password);
		     Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			logger.info("[Inquiry] Checked and verified/created 'inquiry_logs' table.");
		} catch (SQLException e) {
			logger.error("[Inquiry] Failed to initialize database table 'inquiry_logs'", e);
		}
	}

	public long saveInquiryAndGet(@NotNull UUID uuid, @NotNull String name, @NotNull String message) {
		String sql = "INSERT INTO inquiry_logs (uuid, player_name, message, status) VALUES (?, ?, ?, 'PENDING')";
		try (Connection conn = DriverManager.getConnection(url, user, password);
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, uuid.toString());
			pstmt.setString(2, name);
			pstmt.setString(3, message);
			pstmt.executeUpdate();

			try (ResultSet rs = pstmt.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to save inquiry log for UUID: {}", uuid, e);
		}
		return -1;
	}
	
	public void updateInquiryResolution(long id, String answer, String status) {
		String sql = "UPDATE inquiry_logs SET answer = ?, status = ? WHERE id = ?";
		try (Connection conn = DriverManager.getConnection(url, user, password);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, answer);
			pstmt.setString(2, status);
			pstmt.setLong(3, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to update inquiry resolution for ID: {}", id, e);
		}
	}

	public List<Inquiry> getPendingInquiries() {
		List<Inquiry> list = new ArrayList<>();
		String sql = "SELECT id, uuid, player_name, message, answer, status, created_at FROM inquiry_logs WHERE status = 'PENDING'";
		try (Connection conn = DriverManager.getConnection(url, user, password);
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				Inquiry inquiry = new Inquiry(
						rs.getLong("id"),
						UUID.fromString(rs.getString("uuid")),
						rs.getString("player_name"),
						rs.getString("message"),
						rs.getTimestamp("created_at").getTime()
				);
				inquiry.setAnswer(rs.getString("answer"));
				inquiry.setStatus(InquiryStatus.valueOf(rs.getString("status")));
				list.add(inquiry);
			}
		} catch (SQLException e) {
			logger.error("Failed to fetch pending inquiries for cache warm-up", e);
		}
		return list;
	}

	public @NotNull List<String> getLogs(@NotNull UUID uuid) {
		List<String> logs = new ArrayList<>();
		String sql = "SELECT * FROM inquiry_logs WHERE uuid = ? ORDER BY created_at DESC";
		try (Connection conn = DriverManager.getConnection(url, user, password);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, uuid.toString());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
                logs.add("&8[%s] &7%s: &f%s".formatted(
                        rs.getTimestamp("created_at"),
                        rs.getString("status"),
                        rs.getString("message")
                ));
			}
		} catch (SQLException e) {
			logger.error("Failed to get logs for UUID: {}", uuid, e);
		}
		return logs;
	}
}
