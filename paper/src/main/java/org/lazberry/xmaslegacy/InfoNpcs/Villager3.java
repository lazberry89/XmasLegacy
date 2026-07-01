package org.lazberry.xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class Villager3 extends AbstractNpc {
	public Villager3() {
		super(List.of(
				"드디어 우리 아들이 결사대에 들어갔다네.",
				"너무 자랑스럽구만. 빨리 이 대백야가 끝나면 좋겠어.",
				"저기 괴물들때문에 항상 얼어붙은 음식밖에 먹지 못하니..",
				"결사대가 과연 저 괴물들을 물리칠 수 있을진 모르겠지만 유일한 희망인건 변치않는거같군.",
				"자네는 결사대에 관심 없나? 몸도 꽤 탄탄해보이는걸.",
				"누가 되든간에, 빨리 이 시대를 끝내야돼. 불꽃이 점점 약해지고있어.."
		), ColorUtils.chat("&7&l마을주민3"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.VILLAGER3);
	}
}
