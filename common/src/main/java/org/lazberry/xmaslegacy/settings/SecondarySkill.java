package org.lazberry.xmaslegacy.settings;

@SuppressWarnings("DuplicatedCode")
public enum SecondarySkill implements Skill {
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
	MAGIC_BULLET("특수 탄환");

	private final String kor;

	SecondarySkill(String kor) {
		this.kor = kor;
	}

	public SecondarySkill next() {
		return switch (this) {
			case MAGNETIC_FIELD -> KARMA;
			case KARMA -> MAGNETIC_FIELD;

			case TARGET_GUARD -> OVERCHARGE_PRISM;
			case OVERCHARGE_PRISM -> TARGET_GUARD;

			case MADNESS -> TRIPLE_TOMAHAWK;
			case TRIPLE_TOMAHAWK -> MADNESS;

			case COUNTER -> FINISHER;
			case FINISHER -> COUNTER;

			case SNIPE -> MAGIC_BULLET;
			case MAGIC_BULLET -> SNIPE;
		};
	}

	@Override
	public String getKor() {
		return this.kor;
	}

	@Override
	public String getSkillName() {
		return "&e&l[ " + this.getKor() + " ]";
	}
}
