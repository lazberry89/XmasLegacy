package org.lazberry.xmaslegacy.rank.board;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.lazberry.xmaslegacy.rank.RankManager;
import org.lazberry.xmaslegacy.rank.RankType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RankBoardTextTest {
	private static final UUID A = new UUID(0, 1), B = new UUID(0, 2), C = new UUID(0, 3);

	private static String plain(RankType type, List<RankManager.RankData> ranks, Map<UUID, Integer> baseline) {
		return PlainTextComponentSerializer.plainText().serialize(RankBoardText.render(type, ranks, baseline, u -> u.equals(A) ? "<red>x" : "User"));
	}

	@Test
	void showsTrendAndKeepsNamesLiteral() {
		var ranks = List.of(new RankManager.RankData(1, A, 1234567), new RankManager.RankData(2, B, 10), new RankManager.RankData(3, C, 1));
		String text = plain(RankType.DOLLAR, ranks, Map.of(A, 3, B, 1));
		assertTrue(text.contains("<red>x"), text);
		assertTrue(text.contains("1,234,567원 ▲2"), text);
		assertTrue(text.contains("10원 ▼1"), text);
		assertTrue(text.contains("1원 NEW"), text);
		System.out.println(text);
	}

	@Test
	void emptyBoardRenders() {
		assertTrue(plain(RankType.EXP, List.of(), Map.of()).contains("아직 기록이 없습니다."));
	}

	@Test
	void formatsTimeAndLevel() {
		assertEquals("1시간 30분", RankBoardText.format(RankType.PLAYTIME, 90 * 60 * 20));
		assertEquals("59분", RankBoardText.format(RankType.PLAYTIME, 59 * 60 * 20));
		// Level 0 costs 100, level 1 costs 150.
		assertEquals(0, RankManager.levelOf(99));
		assertEquals(1, RankManager.levelOf(100));
		assertEquals(1, RankManager.levelOf(249));
		assertEquals(2, RankManager.levelOf(250));
		assertTrue(RankManager.totalExp(50, 0) > RankManager.totalExp(49, 2500));
	}
}
