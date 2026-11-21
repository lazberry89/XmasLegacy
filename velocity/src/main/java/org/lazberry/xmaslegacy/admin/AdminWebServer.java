package org.lazberry.xmaslegacy.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.Velocity;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.Executors;

/** Read-only admin dashboard: static page from {@code admin-web/} plus a JSON API behind a key login. */
@Slf4j
@Components
public class AdminWebServer implements Initiator {
	private static final String COOKIE = "XL_ADMIN";
	private static final int MAX_BODY = 4096;
	private static final Gson GSON = new Gson();
	private static final Map<String, String> TYPES = Map.of(
			"html", "text/html; charset=utf-8", "css", "text/css; charset=utf-8",
			"js", "text/javascript; charset=utf-8", "woff2", "font/woff2",
			"svg", "image/svg+xml", "png", "image/png");

	private final AdminQueries queries;
	private final AdminKeyStore keys;
	private final AdminSessions sessions;
	private final String bind;
	private final int port;
	private final boolean enabled;
	private HttpServer server;

	@Inject
	public AdminWebServer(@ConfigValue(path = "web.database.url") String url,
	                      @ConfigValue(path = "web.database.user") String user,
	                      @ConfigValue(path = "web.database.password") String password,
	                      @ConfigValue(path = "admin-web.enabled") Boolean enabled,
	                      @ConfigValue(path = "admin-web.bind") String bind,
	                      @ConfigValue(path = "admin-web.port") Integer port) {
		this(new AdminQueries(url, user, password),
				new AdminKeyStore(Velocity.directory().resolve("admin-keys.yml")),
				new AdminSessions(Clock.systemUTC()),
				bind == null ? "0.0.0.0" : bind,
				port == null ? 8095 : port,
				enabled == null || enabled);
	}

	public AdminWebServer(AdminQueries queries, AdminKeyStore keys, AdminSessions sessions, String bind, int port) {
		this(queries, keys, sessions, bind, port, true);
	}

	private AdminWebServer(AdminQueries queries, AdminKeyStore keys, AdminSessions sessions,
	                       String bind, int port, boolean enabled) {
		this.queries = queries;
		this.keys = keys;
		this.sessions = sessions;
		this.bind = bind;
		this.port = port;
		this.enabled = enabled;
	}

	@Override
	public void init() {
		if (!enabled) {
			log.info("Admin web is disabled in config.yml.");
			return;
		}
		if (!keys.hasKeys()) {
			log.warn("Admin web not started: add keys to plugins/xmaslegacy-velocity/admin-keys.yml and restart.");
			return;
		}
		try {
			server = HttpServer.create(new InetSocketAddress(bind, port), 0);
			server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
			server.createContext("/", this::handle);
			server.start();
			log.info("Admin web listening on {}:{}", bind, port());
		} catch (IOException e) {
			log.error("Admin web failed to start on {}:{}", bind, port, e);
		}
	}

	@Override
	public void close() {
		if (server != null) server.stop(0);
	}

	public int port() {
		return server == null ? port : server.getAddress().getPort();
	}

	private void handle(HttpExchange ex) throws IOException {
		try (ex) {
			ex.getResponseHeaders().set("Cache-Control", "no-store");
			ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
			ex.getResponseHeaders().set("Content-Security-Policy", "default-src 'self'; img-src 'self' data:");
			String path = ex.getRequestURI().getRawPath();
			if (path.startsWith("/api/")) api(ex, path);
			else serveStatic(ex, path);
		} catch (Exception e) {
			log.error("Admin web request failed: {}", ex.getRequestURI(), e);
			try {
				json(ex, 500, Map.of("error", "server"));
			} catch (IOException ignored) {
				// response already started
			}
		}
	}

	private void api(HttpExchange ex, String path) throws Exception {
		String method = ex.getRequestMethod();
		if (path.equals("/api/login") && method.equals("POST")) {
			login(ex);
			return;
		}
		String token = cookie(ex);
		if (!sessions.valid(token)) {
			json(ex, 401, Map.of("error", "unauthorized"));
			return;
		}
		if (path.equals("/api/logout") && method.equals("POST")) {
			sessions.revoke(token);
			ex.getResponseHeaders().add("Set-Cookie", COOKIE + "=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0");
			ex.sendResponseHeaders(204, -1);
			return;
		}
		if (!method.equals("GET")) {
			json(ex, 405, Map.of("error", "method"));
			return;
		}

		String[] parts = path.substring("/api/".length()).split("/");
		String id = parts.length > 1 ? parts[1] : null;
		String query = ex.getRequestURI().getQuery();
		Object body = switch (parts[0]) {
			case "overview" -> queries.overview();
			case "users" -> id == null ? queries.users() : queries.user(id);
			case "inquiries" -> queries.inquiries(param(query, "status"));
			case "bags" -> id == null ? queries.bagOwners() : queries.bag(id);
			case "mailbox" -> queries.mailbox();
			case "products" -> queries.products();
			case "advancements" -> queries.advancements();
			default -> null;
		};
		if (body == null) json(ex, 404, Map.of("error", "not found"));
		else json(ex, 200, body);
	}

	private void login(HttpExchange ex) throws IOException {
		String ip = ex.getRemoteAddress().getAddress().getHostAddress();
		if (!sessions.allowAttempt(ip)) {
			json(ex, 429, Map.of("error", "too many attempts"));
			return;
		}
		String key = null;
		try (InputStream in = ex.getRequestBody()) {
			JsonObject o = GSON.fromJson(new String(in.readNBytes(MAX_BODY), StandardCharsets.UTF_8), JsonObject.class);
			if (o != null && o.has("key") && o.get("key").isJsonPrimitive()) key = o.get("key").getAsString();
		} catch (RuntimeException ignored) {
			// malformed body is just a wrong key
		}
		if (!keys.matches(key)) {
			log.warn("Admin web: failed login from {}", ip);
			json(ex, 401, Map.of("error", "invalid key"));
			return;
		}
		String token = sessions.issue();
		ex.getResponseHeaders().add("Set-Cookie", COOKIE + "=" + token
				+ "; HttpOnly; SameSite=Strict; Path=/; Max-Age=" + AdminSessions.TTL.toSeconds());
		log.info("Admin web: login from {}", ip);
		json(ex, 200, Map.of("ok", true));
	}

	private void serveStatic(HttpExchange ex, String path) throws IOException {
		if (path.equals("/")) path = "/index.html";
		String name = path.substring(1);
		if (!ex.getRequestMethod().equals("GET") || name.contains("..") || name.contains("\\") || name.contains("%")) {
			notFound(ex);
			return;
		}
		try (InputStream in = AdminWebServer.class.getClassLoader().getResourceAsStream("admin-web/" + name)) {
			if (in == null) {
				notFound(ex);
				return;
			}
			byte[] bytes = in.readAllBytes();
			String ext = name.substring(name.lastIndexOf('.') + 1);
			ex.getResponseHeaders().set("Content-Type", TYPES.getOrDefault(ext, "application/octet-stream"));
			ex.sendResponseHeaders(200, bytes.length);
			ex.getResponseBody().write(bytes);
		}
	}

	private static void notFound(HttpExchange ex) throws IOException {
		ex.sendResponseHeaders(404, -1);
	}

	private static void json(HttpExchange ex, int status, Object body) throws IOException {
		byte[] bytes = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
		ex.sendResponseHeaders(status, bytes.length);
		ex.getResponseBody().write(bytes);
	}

	private static String cookie(HttpExchange ex) {
		String header = ex.getRequestHeaders().getFirst("Cookie");
		if (header == null) return null;
		for (String part : header.split(";")) {
			String p = part.trim();
			if (p.startsWith(COOKIE + "=")) return p.substring(COOKIE.length() + 1);
		}
		return null;
	}

	private static String param(String query, String name) {
		if (query == null) return null;
		for (String kv : query.split("&")) {
			if (kv.startsWith(name + "=")) return java.net.URLDecoder.decode(kv.substring(name.length() + 1), StandardCharsets.UTF_8);
		}
		return null;
	}
}
