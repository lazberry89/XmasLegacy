package org.lazberry.xmaslegacy.admin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdminWebServerTest {
	@TempDir Path dir;
	AdminWebServer server;
	HttpClient http = HttpClient.newHttpClient();
	String base;

	@BeforeEach
	void start() throws Exception {
		Path keys = dir.resolve("admin-keys.yml");
		Files.writeString(keys, "keys: [\"secret-key\"]\n");
		AdminQueries stub = new AdminQueries("jdbc:none", "", "") {
			@Override public Object users() { return List.of(Map.of("name", "Steve")); }
		};
		server = new AdminWebServer(stub, new AdminKeyStore(keys), new AdminSessions(Clock.systemUTC()), "127.0.0.1", 0);
		server.init();
		base = "http://127.0.0.1:" + server.port();
	}

	@AfterEach
	void stop() {
		server.close();
	}

	private HttpResponse<String> get(String path, String cookie) throws Exception {
		HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path)).GET();
		if (cookie != null) b.header("Cookie", cookie);
		return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> post(String path, String body, String cookie) throws Exception {
		HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		if (cookie != null) b.header("Cookie", cookie);
		return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
	}

	private String login() throws Exception {
		HttpResponse<String> r = post("/api/login", "{\"key\":\"secret-key\"}", null);
		assertEquals(200, r.statusCode());
		String setCookie = r.headers().firstValue("Set-Cookie").orElseThrow();
		assertTrue(setCookie.contains("HttpOnly"));
		assertTrue(setCookie.contains("SameSite=Strict"));
		return setCookie.split(";", 2)[0];
	}

	@Test
	void apiWithoutCookieIs401() throws Exception {
		HttpResponse<String> r = get("/api/users", null);
		assertEquals(401, r.statusCode());
		assertFalse(r.body().contains("Steve"));
	}

	@Test
	void wrongKeyIs401() throws Exception {
		assertEquals(401, post("/api/login", "{\"key\":\"nope\"}", null).statusCode());
	}

	@Test
	void sixthBadLoginIs429() throws Exception {
		for (int i = 0; i < 5; i++) post("/api/login", "{\"key\":\"nope\"}", null);
		assertEquals(429, post("/api/login", "{\"key\":\"secret-key\"}", null).statusCode());
	}

	@Test
	void loginThenUsersReturnsData() throws Exception {
		HttpResponse<String> r = get("/api/users", login());
		assertEquals(200, r.statusCode());
		assertTrue(r.body().contains("Steve"));
	}

	@Test
	void logoutRevokes() throws Exception {
		String cookie = login();
		assertEquals(204, post("/api/logout", "{}", cookie).statusCode());
		assertEquals(401, get("/api/users", cookie).statusCode());
	}

	@Test
	void pathTraversalIs404() throws Exception {
		assertEquals(404, rawGetStatus("/../config.yml"));
		assertEquals(404, rawGetStatus("/admin-web/../config.yml"));
		assertEquals(404, get("/admin-keys.yml", null).statusCode());
	}

	@Test
	void rootServesIndex() throws Exception {
		HttpResponse<String> r = get("/", null);
		assertEquals(200, r.statusCode());
		assertTrue(r.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
	}

	/** HttpClient normalises "..", so send the raw request line. */
	private int rawGetStatus(String path) throws Exception {
		try (Socket s = new Socket("127.0.0.1", server.port())) {
			OutputStream out = s.getOutputStream();
			out.write(("GET " + path + " HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
			out.flush();
			InputStream in = s.getInputStream();
			String status = new String(in.readNBytes(12), StandardCharsets.US_ASCII);
			return Integer.parseInt(status.substring(9, 12));
		}
	}
}
