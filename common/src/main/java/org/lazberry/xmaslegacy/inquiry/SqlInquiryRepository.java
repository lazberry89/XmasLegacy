package org.lazberry.xmaslegacy.inquiry;

import java.util.concurrent.CompletionException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.DatabaseProcessor;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Components.Include(type = ServerType.GLOBAL)
public class SqlInquiryRepository extends DatabaseProcessor {

	@Inject
	public SqlInquiryRepository(@ConfigValue(path = "web.database.url") String url,
	                            @ConfigValue(path = "web.database.user") String user,
	                            @ConfigValue(path = "web.database.password") String password) {
		super(url, user, password);
	}

	@Override
	protected void createTable() {
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

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			log.info("[Inquiry] Checked and verified/created 'inquiry_logs' table.");
		} catch (SQLException e) {
			log.error("[Inquiry] Failed to initialize database table 'inquiry_logs'", e);
		}
	}

	@NonBlocking
	public CompletableFuture<Long> saveInquiryAndGet(@NotNull UUID uuid, @NotNull String name, @NotNull String message) {
		return CompletableFuture.supplyAsync(() -> {
			String sql = "INSERT INTO inquiry_logs (uuid, player_name, message, status) VALUES (?, ?, ?, 'PENDING')";
			try (Connection conn = getConnection();
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
					throw new CompletionException(e); // an id of -1 would be announced as a real inquiry
				}
				throw new IllegalStateException("No inquiry id generated.");
		});
	}

	@NonBlocking
	public CompletableFuture<Void> updateInquiryResolution(long id, String answer, InquiryStatus status) {
		return CompletableFuture.runAsync(() -> {
			synchronized (this) {
				String sql = "UPDATE inquiry_logs SET answer = ?, status = ? WHERE id = ?";
				try (Connection conn = getConnection();
				     PreparedStatement pstmt = conn.prepareStatement(sql)) {
					pstmt.setString(1, answer);
					pstmt.setString(2, status.name());
					pstmt.setLong(3, id);
					pstmt.executeUpdate();
				} catch (SQLException e) {
					log.error("Failed to update inquiry resolution for ID: {}", id, e);
				}
			}
		});
	}

	@NonBlocking
	public CompletableFuture<List<Inquiry>> getPendingInquiries() {
		return CompletableFuture.supplyAsync(() -> {
			List<Inquiry> list = new ArrayList<>();
			String sql = "SELECT id, uuid, player_name, message, answer, status, created_at FROM inquiry_logs WHERE status = 'PENDING'";
			try (Connection conn = getConnection();
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
				log.error("Failed to fetch pending inquiries for cache warm-up", e);
			}
			return list;
		});
	}

	@NonBlocking
	public CompletableFuture<List<String>> getLogs(@NotNull UUID uuid) {
		return CompletableFuture.supplyAsync(() -> {
			List<String> logs = new ArrayList<>();
			String sql = "SELECT * FROM inquiry_logs WHERE uuid = ? ORDER BY created_at DESC";
			try (Connection conn = getConnection();
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
				log.error("Failed to get logs for UUID: {}", uuid, e);
			}
			return logs;
		});
	}
}
