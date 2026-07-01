package org.lazberry.xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class Villager7 extends AbstractNpc {
	public Villager7() {
		super(List.of(
				"요즘 마을이 워낙 숭숭하다며?",
				"내가 앓아눕고 일어난지 얼마 안되서 말이야, 세상이 익숙하지가 않구려.",
				"그건 그렇고, 유독 마을이 어두워보이네.",
				"아 원래 항상 밤 아니냐고?",
				"최소한 영혼의 불빛은 항상 밝았는데, 내 눈이 침침해진건지 어두워졌군.",
				"아 자네 혹시 결사대에 대해 아는가?",
				"..."
		), ColorUtils.chat("&7&l마을주민7"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.VILLAGER7);
	}
}
