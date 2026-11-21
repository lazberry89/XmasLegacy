package org.lazberry.xmaslegacy.admin;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class AdminSessionsTest {
	static class MutableClock extends Clock {
		Instant now = Instant.parse("2026-11-22T00:00:00Z");
		void advance(Duration d) { now = now.plus(d); }
		@Override public ZoneId getZone() { return ZoneId.of("UTC"); }
		@Override public Clock withZone(ZoneId zone) { return this; }
		@Override public Instant instant() { return now; }
	}

	@Test
	void issuedTokenIsValidUntilTtl() {
		MutableClock clock = new MutableClock();
		AdminSessions s = new AdminSessions(clock);
		String token = s.issue();
		assertTrue(s.valid(token));

		clock.advance(AdminSessions.TTL.plusSeconds(1));
		assertFalse(s.valid(token));
	}

	@Test
	void revokedTokenIsInvalid() {
		AdminSessions s = new AdminSessions(new MutableClock());
		String token = s.issue();
		s.revoke(token);
		assertFalse(s.valid(token));
	}

	@Test
	void unknownAndNullTokensInvalid() {
		AdminSessions s = new AdminSessions(new MutableClock());
		assertFalse(s.valid("nope"));
		assertFalse(s.valid(null));
		s.revoke(null);
	}

	@Test
	void rateLimitsFiveAttemptsPerMinute() {
		MutableClock clock = new MutableClock();
		AdminSessions s = new AdminSessions(clock);
		for (int i = 0; i < 5; i++) assertTrue(s.allowAttempt("1.2.3.4"));
		assertFalse(s.allowAttempt("1.2.3.4"));
		assertTrue(s.allowAttempt("5.6.7.8"));

		clock.advance(Duration.ofSeconds(61));
		assertTrue(s.allowAttempt("1.2.3.4"));
	}
}
