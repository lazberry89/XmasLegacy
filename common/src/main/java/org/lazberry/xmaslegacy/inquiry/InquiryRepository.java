package org.lazberry.xmaslegacy.inquiry;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Registry.Include(type = ServerType.GLOBAL)
public class InquiryRepository {
	private final @NotNull String url = "jdbc:sqlite:plugins/XmasLegacy/database.db";
	private final @NotNull String user = "root";
	private final @NotNull String password = "password";
	private static final @NotNull Logger logger = LoggerFactory.getLogger(InquiryRepository.class);

	public InquiryRepository() {
		createDatabaseFolder();
		createNewTable();
	}

	public void saveInquiry(@NotNull UUID uuid, @NotNull String name, @NotNull String message) {
		String sql = "INSERT INTO inquiry_logs (uuid, player_name, message, status) VALUES (?, ?, ?, 'PENDING')";
		try (Connection conn = DriverManager.getConnection(url, user, password);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, uuid.toString());
			pstmt.setString(2, name);
			pstmt.setString(3, message);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to save inquiry log for UUID: {}", uuid, e);
		}
	}

	private void createDatabaseFolder() {
		File dbFolder = new File("plugins/XmasLegacy");
		if (!dbFolder.exists()) {
			if (dbFolder.mkdirs()) {
				logger.info("[Inquiry] Successfully created database folder directory.");
			}
		}
	}

	private void createNewTable() {
		String sql = """
            CREATE TABLE IF NOT EXISTS inquiry_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid TEXT NOT NULL,
                player_name TEXT NOT NULL,
                message TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
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

	public void updateStatus(@NotNull UUID uuid, @NotNull String status) {
		String sql = "UPDATE inquiry_logs SET status = ? WHERE uuid = ? AND status = 'PENDING' ORDER BY created_at DESC LIMIT 1";
		try (Connection conn = DriverManager.getConnection(url, user, password);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setString(2, uuid.toString());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to update status for UUID: {}", uuid, e);
		}
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
