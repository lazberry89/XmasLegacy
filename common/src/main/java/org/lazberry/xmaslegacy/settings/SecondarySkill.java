package org.lazberry.xmaslegacy.settings;

public enum SecondarySkill {
	//Defender
	MAGNETIC_FIELD("자기장 영역"),
	KARMA("카르마");

	private final String kor;

	SecondarySkill(String kor) {
		this.kor = kor;
	}

	public SecondarySkill next() {
		return switch (this) {
			case MAGNETIC_FIELD -> KARMA;
			case KARMA -> MAGNETIC_FIELD;
		};
	}

	public String getKorName() {
		return this.kor;
	}

	public static SecondarySkill getByOrdinal(int ordinal) {
		for (SecondarySkill skill : values()) {
			if (skill.ordinal() == ordinal) {
				return skill;
			}
		}
		return null;
	}

	public String getSkillName() {
		return "[ " + this.getKorName() + " ]";
	}
}
