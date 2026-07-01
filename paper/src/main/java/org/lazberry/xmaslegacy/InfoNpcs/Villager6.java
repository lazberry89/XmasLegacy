package org.lazberry.xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class Villager6 extends AbstractNpc {
	public Villager6() {
		super(List.of(
				"결사대..? 난 관심없어.",
				"이번에 내 딸이 수집가가 됐다고 얼마나 좋아했는데.",
				"파티 채집을 나서서 3일째 돌아오질 않아.",
				"하필 제일 위험한 거울신전으로 갔다더군.",
				"뭔 암호를 찾았다고 그러면서 말이야.",
				"다 필요없어..딸도 없이 무슨 낙으로 여기서 살아.."
		), ColorUtils.chat("&7&l마을주민6"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.VILLAGER6);
	}
}
