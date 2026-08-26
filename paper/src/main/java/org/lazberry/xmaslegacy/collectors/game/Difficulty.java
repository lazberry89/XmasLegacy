package org.lazberry.xmaslegacy.collectors.game;

import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Sound;
import org.lazberry.xmaslegacy.collectors.hunter.Hunter;
import org.lazberry.xmaslegacy.collectors.hunter.HunterBug;
import org.lazberry.xmaslegacy.collectors.hunter.PhaseHunter;
import org.lazberry.xmaslegacy.collectors.hunter.Silence;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum Difficulty {
	PEACEFUL(42, 2, 4, 20 * 60 * 5L, BossBar.Color.GREEN, Sound.BLOCK_FIRE_EXTINGUISH, HunterBug.class),
	EXCITING(25, 5, 7, 20 * 60 * 7L, BossBar.Color.YELLOW, Sound.ENTITY_ENDER_DRAGON_GROWL, PhaseHunter.class),
	HORROR(22, 9, 12, 20 * 60 * 10L, BossBar.Color.RED, Sound.ENTITY_WARDEN_ROAR, Silence.class);

	private final int dropCount;
	private final int minimumHit;
	private final int maximumHit;
	private final long duration;
	private final BossBar.Color color;
	private final Sound overSound;
	private final Class<? extends Hunter> hunter;

	public int getRandomHit() {
		return ThreadLocalRandom.current().nextInt(minimumHit, maximumHit + 1);
	}
	public int getDropCount() {
		return ThreadLocalRandom.current().nextInt(dropCount - 2, dropCount + 2);
	}

	Difficulty(int dropCount, int minHit, int maxHit, long duration, BossBar.Color color, Sound overSound, Class<? extends Hunter> hunter) {
		this.dropCount = dropCount;
		this.minimumHit = minHit;
		this.maximumHit = maxHit;
		this.duration = duration;
		this.color = color;
		this.overSound = overSound;
		this.hunter = hunter;
	}
}
