package org.lazberry.xmaslegacy.settings;

import org.jetbrains.annotations.NotNull;

public interface SkillSet {

	/**
	 * Each skill should have Korean name for UI/UX.
	 * @return Korean name of skill
	 */
	@NotNull String getKor();

	/**
	 * Indicates skill's original name.
	 * @return Skill's original name
	 */
	@NotNull String getSkillName();
}
