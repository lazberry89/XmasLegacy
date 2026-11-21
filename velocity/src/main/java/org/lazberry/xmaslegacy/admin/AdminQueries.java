package org.lazberry.xmaslegacy.admin;

import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only queries behind the admin page. Every method only runs SELECT statements. */
public class AdminQueries {
	private static final String NAME = "COALESCE(u.name, LEFT(%s, 8))";
	private static final Gson GSON = new Gson();

	private final String url;
	private final String user;
	private final String password;

	public AdminQueries(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public Object overview() throws SQLException {
		Map<String, Object> out = new LinkedHashMap<>();
		Map<String, Object> totals = rows("SELECT COUNT(*) AS users, COALESCE(SUM(dollars), 0) AS totalDollars FROM users").get(0);
		out.putAll(totals);
		out.put("pendingInquiries", rows("SELECT COUNT(*) AS c FROM inquiry_logs WHERE status = 'PENDING'").get(0).get("c"));
		out.put("recentInquiries", rows("SELECT id, uuid, player_name, message, answer, status, created_at " +
				"FROM inquiry_logs ORDER BY id DESC LIMIT 5"));
		return out;
	}

	public Object users() throws SQLException {
		return rows("SELECT uuid, name, role, level, dollars, playTime, tier, mastery, equipPrefix FROM users ORDER BY name");
	}

	public Object user(String uuid) throws SQLException {
		List<Map<String, Object>> found = rows("SELECT * FROM users WHERE uuid = ?", uuid);
		if (found.isEmpty()) return null;
		Map<String, Object> u = found.get(0);
		Object prefixes = u.get("availablePrefix");
		u.put("availablePrefix", prefixes == null ? List.of() : GSON.fromJson(prefixes.toString(), List.class));
		return u;
	}

	public Object inquiries(String status) throws SQLException {
		String sql = "SELECT id, uuid, player_name, message, answer, status, created_at FROM inquiry_logs";
		return status == null || status.isBlank()
				? rows(sql + " ORDER BY id DESC LIMIT 500")
				: rows(sql + " WHERE status = ? ORDER BY id DESC LIMIT 500", status);
	}

	public Object bagOwners() throws SQLException {
		List<Map<String, Object>> out = new ArrayList<>();
		for (Map<String, Object> r : rows("SELECT b.uuid, " + NAME.formatted("b.uuid") + " AS name, b.contents " +
				"FROM bags b LEFT JOIN users u ON u.uuid = b.uuid ORDER BY name")) {
			Object contents = r.remove("contents");
			r.put("itemCount", ItemYamlParser.parse(contents == null ? null : contents.toString()).size());
			out.add(r);
		}
		return out;
	}

	public Object bag(String uuid) throws SQLException {
		List<Map<String, Object>> found = rows("SELECT b.uuid, " + NAME.formatted("b.uuid") + " AS name, b.contents " +
				"FROM bags b LEFT JOIN users u ON u.uuid = b.uuid WHERE b.uuid = ?", uuid);
		if (found.isEmpty()) return null;
		return withItems(found.get(0));
	}

	public Object mailbox() throws SQLException {
		List<Map<String, Object>> out = new ArrayList<>();
		for (Map<String, Object> r : rows("SELECT m.id, m.uuid, " + NAME.formatted("m.uuid") + " AS name, m.contents " +
				"FROM bag_mailbox m LEFT JOIN users u ON u.uuid = m.uuid ORDER BY m.id DESC")) {
			out.add(withItems(r));
		}
		return out;
	}

	public Object products() throws SQLException {
		return rows("SELECT p.id, p.receiver, " + NAME.formatted("p.receiver") + " AS name, p.product, p.granted " +
				"FROM products p LEFT JOIN users u ON u.uuid = p.receiver ORDER BY p.id DESC");
	}

	public Object advancements() throws SQLException {
		Map<String, Map<String, Object>> players = new LinkedHashMap<>();
		for (Map<String, Object> r : rows("SELECT p.UUID AS uuid, p.Name AS name, a.Namespace AS namespace, " +
				"a.`Key` AS `key`, a.Progression AS progression FROM Players p " +
				"LEFT JOIN Advancements a ON a.TeamID = p.TeamID ORDER BY p.Name, a.Namespace, a.`Key`")) {
			Map<String, Object> player = players.computeIfAbsent((String) r.get("uuid"), k -> {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("uuid", r.get("uuid"));
				m.put("name", r.get("name"));
				m.put("done", new ArrayList<Map<String, Object>>());
				return m;
			});
			if (r.get("key") != null) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> done = (List<Map<String, Object>>) player.get("done");
				done.add(Map.of("namespace", r.get("namespace"), "key", r.get("key"), "progression", r.get("progression")));
			}
		}
		return new ArrayList<>(players.values());
	}

	private Map<String, Object> withItems(Map<String, Object> row) {
		Object contents = row.remove("contents");
		row.put("items", ItemYamlParser.parse(contents == null ? null : contents.toString()));
		return row;
	}

	private List<Map<String, Object>> rows(String sql, Object... params) throws SQLException {
		try (Connection c = DriverManager.getConnection(url, user, password);
		     PreparedStatement ps = c.prepareStatement(sql)) {
			c.setReadOnly(true);
			for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
			try (ResultSet rs = ps.executeQuery()) {
				ResultSetMetaData md = rs.getMetaData();
				List<Map<String, Object>> out = new ArrayList<>();
				while (rs.next()) {
					Map<String, Object> row = new LinkedHashMap<>();
					for (int i = 1; i <= md.getColumnCount(); i++) {
						Object v = rs.getObject(i);
						row.put(md.getColumnLabel(i), v instanceof Timestamp t ? t.getTime() : v);
					}
					out.add(row);
				}
				return out;
			}
		}
	}
}
