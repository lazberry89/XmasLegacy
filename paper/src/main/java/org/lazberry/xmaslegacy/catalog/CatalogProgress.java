package org.lazberry.xmaslegacy.catalog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One player's catalog state: per catalog type a 64-bit mask where bit n means slot n is registered.
 * Slot positions are fixed by code, so a registered item never moves to another slot.
 */
public class CatalogProgress {
	public static final int MAX_SLOTS = 64;

	private final Map<CatalogType, Long> bits = new ConcurrentHashMap<>();

	static void checkSlot(int slot) {
		if (slot < 0 || slot >= MAX_SLOTS) throw new IllegalArgumentException("Catalog slot must be 0-63: " + slot);
	}

	/** Used by the repository while loading. */
	void setBits(CatalogType type, long value) {
		bits.put(type, value);
	}

	public long getBits(CatalogType type) {
		return bits.getOrDefault(type, 0L);
	}

	public boolean isRegistered(CatalogType type, int slot) {
		checkSlot(slot);
		return (getBits(type) >>> slot & 1L) == 1L;
	}

	/** Marks the slot in memory. Returns false if it was already registered. */
	public boolean register(CatalogType type, int slot) {
		checkSlot(slot);
		long bit = 1L << slot;
		long before = bits.getOrDefault(type, 0L);
		bits.merge(type, bit, (old, add) -> old | add);
		return (before & bit) == 0;
	}

	/** Number of registered slots of one catalog type. */
	public int count(CatalogType type) {
		return Long.bitCount(getBits(type));
	}
}
