package org.lazberry.xmaslegacy.settings;

import org.jetbrains.annotations.NotNull;

public enum SecondarySkillSet implements SkillSet {
	//Defender
	SOUL_STEAL("영혼 수확"),
	KARMA("카르마"),

	//Guardian
	TARGET_GUARD("타겟 가드"),
	OVERCHARGE_PRISM("과충전 프리즘"),

	//Berserker
	ULTRA_MADNESS("광폭화"),
	TRIPLE_TOMAHAWK("트리플 토마호크"),

	//Fighter
	COUNTER("반격"),
	FINISHER("피니셔"),

	//Sniper
	SNIPE("저격"),
	MAGIC_BULLET("특수 탄환"),
	FIRE_BULLET("발사"),

	//Ranger
	CHAINING("체이닝"),
	PRISM_LASER("프리즘 광선"),

	//Trapper
	SHOTGUN("산탄총"),
	CHAIN_GRAB("사슬 그랩"),

	//Wizard
	MANA_ORB("마나구슬"),
	ARCHMAGE_ZONE("아크메이지 존");

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
