package org.lazberry.xmasLegacy.Skill;

public enum BasicSkills {
	SHOCK_DART,
	BACK_DASH,

	SHARP_SWEEPING,
	TAUNT,

	DAGGER_RUSH,
	SMOKE,

	TOMAHAWK,
	BLOOD_FRENZY;

	BasicSkills() {}

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
		};
	}
	public String getSkillName() {
		return "[ " + this.name() + " ]";
	}
}
