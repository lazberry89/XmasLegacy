package org.lazberry.xmaslegacy.infoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.List;

public class CosmeticNpc extends AbstractNpc {
	public CosmeticNpc() {
		super(List.of(
				"치장품 상점에 온것을 환영하네!",
				"여기선 자네가 원하는 치장품을 즉시 선택구매 할 수 있다네.",
				"구매버튼을 우클릭하면 아이템이 바로 지급되지.",
				"즐겨보게!"
				),
				ColorUtils.chat("&c&l치장품상점 주인"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.COSMETIC);
	}
}
