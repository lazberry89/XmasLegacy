package org.lazberry.xmaslegacy.settings;

import org.jetbrains.annotations.NotNull;

public enum SecondarySkillSet implements SkillSet {
	//Defender
	MAGNETIC_FIELD("자기장 영역"),
	KARMA("카르마"),

	//Guardian
	TARGET_GUARD("타겟 가드"),
	OVERCHARGE_PRISM("과충전 프리즘"),

	//Berserker
	MADNESS("광기"),
	TRIPLE_TOMAHAWK("트리플 토마호크"),

	//Fighter
	COUNTER("반격"),
	FINISHER("피니셔"),

	//Sniper
	SNIPE("저격"),
	MAGIC_BULLET("특수 탄환"),

	//Ranger
	PRISM_LASER("프리즘 광선"),
	CHAINING("체이닝");

	private final @NotNull String kor;

	SecondarySkillSet(@NotNull String kor) {
		this.kor = kor;
	}

	@Override
	public @NotNull String getKor() {
		return this.kor;
	}

	@Override
	public @NotNull String getSkillName() {
		return "&e&l[ " + this.getKor() + " ]";
	}
}
