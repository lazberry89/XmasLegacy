package org.lazberry.xmaslegacy.Inquiry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InquiryRepository {
	private final String url = "jdbc:mysql://localhost:3306/db_name";
	private final String user = "root";
	private final String password = "password";

	public void saveInquiry(UUID uuid, String name, String message) {
		String sql = "INSERT INTO inquiry_logs (uuid, player_name, message, status) VALUES (?, ?, ?, 'PENDING')";
		try (Connection conn = DriverManager.getConnection(url, user, password);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, uuid.toString());
			pstmt.setString(2, name);
			pstmt.setString(3, message);
			pstmt.executeUpdate();
		} catch (SQLException e) { e.printStackTrace(); }
	}

	// 상태 업데이트 (RESOLVED 등)
	public void updateStatus(UUID uuid, String status) {
		String sql = "UPDATE inquiry_logs SET status = ? WHERE uuid = ? AND status = 'PENDING' ORDER BY created_at DESC LIMIT 1";
		try (Connection conn = DriverManager.getConnection(url, user, password);
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setString(2, uuid.toString());
			pstmt.executeUpdate();
		} catch (SQLException e) { e.printStackTrace(); }
	}

	// 특정 유저의 로그 가져오기
	public List<String> getLogs(UUID uuid) {
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
		} catch (SQLException e) { e.printStackTrace(); }
		return logs;
	}
}
