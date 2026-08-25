package org.lazberry.xmaslegacy.infoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.List;

public class CenterNpc extends AbstractNpc {

	public CenterNpc() {
		super(List.of(
				"반갑네. 안내인의 소개로 왔지?",
				"여기 마을도 한번 둘러보게. 얼어붙어버렸지만 좋은게 얻어갈게 많아.",
				"만약 직업을 가질거라면, 마을회관을 가보도록해.",
				"마을 회관은 얼어붙은 항구에 있을게야.",
				"자네의 친구들도 돈을 벌려고 이미 갔다네.",
				"옆에 보이는 책을 만져서 이동해보게!"
		), ColorUtils.chat("&b&l마을 이장"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.ROLE);
	}
}
