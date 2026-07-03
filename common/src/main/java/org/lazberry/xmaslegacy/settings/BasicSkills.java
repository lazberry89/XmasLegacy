package org.lazberry.xmaslegacy.settings;

import org.jetbrains.annotations.NotNull;

public enum BasicSkills implements SkillSet {
	SHOCK_DART("충격화살"),
	BACK_DASH("백대시"),

	SHARP_SWEEPING("칼날 돌진"),
	TAUNT("광역 도발"),

	DAGGER_RUSH("돌진기"),
	SMOKE("연막탄"),

	TOMAHAWK("토마호크"),
	BLOOD_FRENZY("프렌지"),

    COMPACT_POINT("극점"),
    GRAVITY("중력장"),

	COMPACT_HEAL("컴팩트 힐"),
	STEROID("불꽃의 가호"),

	RADIUS_HARVEST("풍요의 손길"),
	SPEED_GROWER("시간의 축복"),

	CHAIN_MINING("연쇄 광질"),
	ORE_EYE("광부의 눈"),

	ETERNAL_POSE("회귀의 바늘"),
	TRUTH_EYE("에테르의 눈"),

	OPEN_STOCKS("구매품 보기"),
	SELL_ITEMS("판매하기"),

	FIX("수리하기"),
	TEMP_BUFF("일시 버프");

	private final @NotNull String korName;

	BasicSkills(@NotNull String korName) {
		this.korName = korName;
	}

	@Override
	public @NotNull String getKor() {
		return korName;
	}

	public @NotNull String getSkillName() {
		return "[ " + this.getKor() + " ]";
	}
}
