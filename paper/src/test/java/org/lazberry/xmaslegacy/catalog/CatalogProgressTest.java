package org.lazberry.xmaslegacy.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CatalogProgressTest {
	@Test
	void registersEachSlotOnce() {
		var p = new CatalogProgress();
		assertTrue(p.register(CatalogType.FARM, 26));
		assertFalse(p.register(CatalogType.FARM, 26));
		assertTrue(p.isRegistered(CatalogType.FARM, 26));
		assertFalse(p.isRegistered(CatalogType.FISH, 26));
	}

	@Test
	void survivesJsonIncludingTheHighestBit() {
		var p = new CatalogProgress();
		p.register(CatalogType.MINE, 63);
		p.register(CatalogType.MINE, 0);
		p.setRewardGranted(CatalogType.MINE, true);
		var read = CatalogProgress.fromJson(p.toJson());
		assertTrue(read.isSameAs(p));
		assertTrue(read.register(CatalogType.MINE, 1)); // still writable
	}

	@Test
	void mergeKeepsBothSidesAndCopyIsIndependent() {
		var redis = new CatalogProgress();
		redis.register(CatalogType.FARM, 1);
		var db = new CatalogProgress();
		db.register(CatalogType.FARM, 2);
		db.setRewardGranted(CatalogType.FARM, true);

		var merged = redis.copy();
		merged.merge(db);
		assertEquals(0b110L, merged.getBits(CatalogType.FARM));
		assertTrue(merged.isRewardGranted(CatalogType.FARM));
		assertEquals(0b10L, redis.getBits(CatalogType.FARM));
	}

	@Test
	void readsJsonWrittenBeforeRewardsExisted() {
		var p = CatalogProgress.fromJson("{\"bits\":{\"FARM\":11}}");
		assertTrue(p.isRegistered(CatalogType.FARM, 3));
		assertFalse(p.isRewardGranted(CatalogType.FARM));
	}
}
