package org.lazberry.xmaslegacy.catalog;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * One player's catalog state: per catalog type a 64-bit mask where bit n means slot n is registered.
 * Slot positions are fixed by the sample inventory, so a registered item never moves to another slot.
 */
public class CatalogProgress {
	public static final int MAX_SLOTS = 64;
	private static final Gson gson = new Gson();

	// Declared as ConcurrentHashMap, not Map: Gson picks the implementation from the declared type.
	private final ConcurrentHashMap<CatalogType, Long> bits = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<CatalogType, Boolean> rewardGranted = new ConcurrentHashMap<>();

	public static void checkSlot(int slot) {
		if (slot < 0 || slot >= MAX_SLOTS) throw new IllegalArgumentException("Catalog slot must be 0-63: " + slot);
	}

	public static CatalogProgress fromJson(String json) {
		return gson.fromJson(json, CatalogProgress.class);
	}

	public String toJson() {
		return gson.toJson(this);
	}

	public @NotNull CatalogProgress copy() {
		CatalogProgress copy = new CatalogProgress();
		copy.bits.putAll(bits);
		copy.rewardGranted.putAll(rewardGranted);
		return copy;
	}

	/** Adds everything the other progress has. Nothing is ever removed, so the newer of the two always survives. */
	public void merge(@NotNull CatalogProgress other) {
		other.bits.forEach((type, value) -> bits.merge(type, value, (a, b) -> a | b));
		other.rewardGranted.forEach((type, value) -> rewardGranted.merge(type, value, Boolean::logicalOr));
	}

	public boolean isSameAs(@NotNull CatalogProgress other) {
		for (CatalogType type : CatalogType.values()) {
			if (getBits(type) != other.getBits(type) || isRewardGranted(type) != other.isRewardGranted(type)) return false;
		}
		return true;
	}

	public boolean isRewardGranted(CatalogType type) {
		return rewardGranted.getOrDefault(type, false);
	}

	public void setRewardGranted(CatalogType type, boolean isGranted) {
		rewardGranted.put(type, isGranted);
	}

	/** Used by the repository while loading. */
	public void setBits(CatalogType type, long value) {
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
		boolean[] added = {false};
		bits.compute(type, (t, old) -> {
			long current = old == null ? 0L : old;
			added[0] = (current & bit) == 0;
			return current | bit;
		});
		return added[0];
	}
}
