package org.lazberry.xmaslegacy.collectors.game;

import lombok.Getter;

public enum Difficulty {
	PEACEFUL(5, 10, 20 * 60 * 5L),
	EXCITING(15, 20, 20 * 60 * 7L),
	HORROR(25, 30, 20 * 60 * 10L);

	private final @Getter int minimumHit;
	private final @Getter int maximumHit;
	private final @Getter long duration;

	Difficulty(int minHit, int maxHit, long duration) {
		this.minimumHit = minHit;
		this.maximumHit = maxHit;
		this.duration = duration;
	}
}
