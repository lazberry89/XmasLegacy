package org.lazberry.xmaslegacy.settings;

public enum BasicSkills {
	SHOCK_DART("충격화살"),
	BACK_DASH("백대시"),

	SHARP_SWEEPING("칼날 돌진"),
	TAUNT("광역 도발"),

	DAGGER_RUSH("돌진기"),
	SMOKE("연막탄"),

	TOMAHAWK("토마호크"),
	BLOOD_FRENZY("프렌지"),

    COMPACT_INSANELY("극점"),
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

	private final String korName;

	BasicSkills(String korName) {
		this.korName = korName;
	}

	public String getKorName() {
		return korName;
	}

	@SuppressWarnings("DuplicatedCode")
	public BasicSkills next() {
		return switch (this) {
			// Archer
			case SHOCK_DART -> BACK_DASH;
			case BACK_DASH -> SHOCK_DART;

			// Knight
			case SHARP_SWEEPING -> TAUNT;
			case TAUNT -> SHARP_SWEEPING;

			// Rogue
			case DAGGER_RUSH -> SMOKE;
			case SMOKE -> DAGGER_RUSH;

			// Warrior
			case TOMAHAWK -> BLOOD_FRENZY;
			case BLOOD_FRENZY -> TOMAHAWK;

			// Mage
            case COMPACT_INSANELY -> GRAVITY;
            case GRAVITY -> COMPACT_INSANELY;

			// Priest
			case COMPACT_HEAL -> STEROID;
			case STEROID -> COMPACT_HEAL;

			// Farmer
			case RADIUS_HARVEST -> SPEED_GROWER;
			case SPEED_GROWER -> RADIUS_HARVEST;

			// Miner
			case CHAIN_MINING -> ORE_EYE;
			case ORE_EYE -> CHAIN_MINING;

			// Gatherer
			case ETERNAL_POSE -> TRUTH_EYE;
			case TRUTH_EYE -> ETERNAL_POSE;

			//Merchant
			case OPEN_STOCKS -> SELL_ITEMS;
			case SELL_ITEMS -> OPEN_STOCKS;

			case FIX -> TEMP_BUFF;
			case TEMP_BUFF -> FIX;
		};
	}
	public String getSkillName() {
		return "[ " + this.getKorName() + " ]";
	}
}
