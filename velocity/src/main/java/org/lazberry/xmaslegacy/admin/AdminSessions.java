package org.lazberry.xmaslegacy.admin;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory admin sessions (lost on proxy restart) plus a per-IP login attempt limit. */
public class AdminSessions {
	public static final Duration TTL = Duration.ofHours(12);
	static final int MAX_ATTEMPTS = 5;
	static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(1);

	private final Clock clock;
	private final SecureRandom random = new SecureRandom();
	private final Map<String, Instant> sessions = new ConcurrentHashMap<>();
	private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

	public AdminSessions(Clock clock) {
		this.clock = clock;
	}

	public String issue() {
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		sessions.put(token, clock.instant().plus(TTL));
		return token;
	}

	public boolean valid(String token) {
		if (token == null) return false;
		Instant now = clock.instant();
		sessions.values().removeIf(exp -> !exp.isAfter(now));
		return sessions.containsKey(token);
	}

	public void revoke(String token) {
		if (token != null) sessions.remove(token);
	}

	/** Records a login attempt from {@code ip}; false once it has made too many within the window. */
	public boolean allowAttempt(String ip) {
		Instant now = clock.instant();
		Deque<Instant> q = attempts.computeIfAbsent(ip, k -> new ArrayDeque<>());
		synchronized (q) {
			while (!q.isEmpty() && !q.peekFirst().isAfter(now.minus(ATTEMPT_WINDOW))) q.pollFirst();
			if (q.size() >= MAX_ATTEMPTS) return false;
			q.addLast(now);
			return true;
		}
	}
}
