package org.lazberry.xmaslegacy.collectors.drop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum CollectorLoot {
	// 1열 (TRASH) - 무게 적음, 가격 낮음
	JUNK_PAPER("폐지", Material.DRIED_KELP, Value.TRASH, 2, 400),
	STRING("실", Material.STRING, Value.TRASH, 2, 600),
	BLOOD_EYE("피묻은 눈알", Material.SPIDER_EYE, Value.TRASH, 3, 900),

	// 2열 (RARE) - 무게 중간, 가성비 적절
	COPPER_NUGGET("구리 조각", Material.COPPER_NUGGET, Value.RARE, 6, 2000),
	IRON_NUGGET("철 조각", Material.IRON_NUGGET, Value.RARE, 8, 2800),
	GOLD_NUGGET("금 조각", Material.GOLD_NUGGET, Value.RARE, 10, 3600),

	// 3열 (PRECIOUS) - 무거움, 높은 가격
	IRON_INGOT("철괴", Material.IRON_INGOT, Value.PRECIOUS, 20, 7500),
	GOLD_INGOT("금괴", Material.GOLD_INGOT, Value.PRECIOUS, 25, 10000),
	DIAMOND("다이아몬드", Material.DIAMOND, Value.PRECIOUS, 15, 14000),

	// 4열 (NOBLE) - 매우 무겁거나 초고가치
	NETHER_STAR("네더의 별", Material.NETHER_STAR, Value.NOBLE, 35, 45000),
	AMETHYST_CLUSTER("자수정 결정", Material.AMETHYST_CLUSTER, Value.NOBLE, 30, 30000),

	// 5열 (SPECIAL) - 무게 0, 유틸 아이템
	TIME_AMPLIFIER("시간 증폭기", Material.TRIAL_KEY, Value.SPECIAL, 0, 0),
	ESCAPER("탈출기", Material.OMINOUS_TRIAL_KEY, Value.SPECIAL, 0, 0);

	private final String name;
	private final Material material;
	private final Value value;
	private final int weight;
	private final int price;
}
