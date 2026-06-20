package xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class Villager5 extends AbstractNpc {
	public Villager5() {
		super(List.of(
				"자네, 시장 갔다오는 길인가?",
				"지금 태양초 가격이 너무 올랐어.",
				"정부는 인플레이션 관리도 안하고 뭐하는거야..",
				"태양초 없이 어떻게 살라고??",
				"마을이 망해가는구만.."
		), ColorUtils.chat("&7&l마을주민5"), Sound.ENTITY_VILLAGER_AMBIENT);
	}
}
