package org.lazberry.xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class Villager2 extends AbstractNpc {
	public Villager2() {
		super(List.of(
				"자네 그거 들었나?",
				"이번에 &c상부&l에서 드디어 결사대 모집이 승인됐다던데.",
				"근데 누가 결사대를 하는지 공식적인게 하나도 없구먼그래?",
				"아무래도 강한자들을 뽑지 않을까 싶은데.",
				"이번에 &6등급제&f를 도입했다하지 않았나? 높은 인원들로 데려가지 않을까 싶은데.",
				"누가 결사대가 될지 궁금해지는구만.",
				"빨리 이 &b대백야&f 시대가 끝나면 좋겠는데..",
				"..."
		), ColorUtils.chat("&7&l마을주민2"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.VILLAGER2);
	}
}
