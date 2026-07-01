package org.lazberry.xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class Villager4 extends AbstractNpc {
	public Villager4() {
		super(List.of(
				"오 마침 잘왔네. 말해줄게 있었거든.",
				"혹시 마녀를 찾아가봤나?",
				"안가봤다면, 그냥 가지말게.",
				"마을에 자꾸 이상한 소문이 돌더군, &4재앙&f이라면서.",
				"뭐가 재앙이냐고? 확실히는 모르지만, 결사대를 반대하는 사람들이 생긴다더군.",
				"아마 저 마녀 때문이 아닌가 싶어.",
				"왜 드디어 자유로워질려 하는데, 그걸 막는건지. 도무지 이해할 수 없군.",
				"결사대는 우리의 마지막 희망이자 꿈인데..."
		), ColorUtils.chat("&7&l마을주민4"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.VILLAGER4);
	}
}
