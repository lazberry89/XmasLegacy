package org.lazberry.xmaslegacy.prefix;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.utils.ColorUtils;

@Getter
public enum MissionPrefix implements ServerPrefix {
	FIRST_JOINER("&#F0EEA8&l[&#F3E286&l세&#F6D565&l계&#F9C943&l최&#FCBC22&l초&#FFB000&l]",
			"서버에 처음으로 접속하여 획득", false, true),
	VIP("&#FC5799&l[&#FD6D73&lV&#FE844D&lI&#FE9A26&lP&#FFB000&l]",
			"웹후원 1회를 달성하여 획득 가능", true, true),
	V_VIP("&#FC5799&l[&#FD697A&lV&#FD7B5C&lV&#FE8C3D&lI&#FE9E1F&lP&#FFB000&l]",
			"웹후원 5회를 달성하여 획득 가능", true, true),
	THOUSAND_RICH("&#57FC8A[&#79F96E천&#9AF653만&#BCF437부&#DDF11C자&#FFEE00]",
			"서버 최초로 1천만원을 모으세요.", false, true),
	DIRT_SAND_GRAVEL("&#FAAF0A[&#E29F0A흙&#CA8E09흙&#B37E09모&#9B6D08래&#835D08모&#6B4C07래&#6F490F자&#734617갈&#76441E자&#7A4126갈&#7E3E2E]",
			"1000블록을 캐세요.", true, true),
	FROZE_TO_DEATH("&#99E2F8[&#8CDAF9L&#7ED3F9e&#71CBFAt &#56BCFCI&#48B4FCt &#2DA4FEG&#209DFEo&#1295FF]",
			"", false, false),
	;

	private final String prefix;
	private final String description;
	private final boolean reusable;
	private final boolean  isPublic;

	MissionPrefix(String prefix, String description, boolean reusable, boolean isPublic) {
		this.prefix = prefix;
        this.description = description;
        this.reusable = reusable;
        this.isPublic = isPublic;
    }

	@Override
	public @NotNull Component prefix() {
		return ColorUtils.chat(this.prefix);
	}
}
