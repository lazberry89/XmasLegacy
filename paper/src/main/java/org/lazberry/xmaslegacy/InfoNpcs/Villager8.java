package org.lazberry.xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class Villager8 extends AbstractNpc {
	public Villager8() {
		super(List.of(
				"윽..머리가 아프구만.",
				"요즘 마을에 뭔일 있나? 사람들 분위기가 심상치 않구려.",
				"그나저나, 머리는 왜이렇게 아픈지 모르곘네.",
				"혹시 뭐 타이레놀같은거 들고다니나?",
				"에휴, 나이들었으니 당연히 아픈거겠지.",
				"조용히 살다 가야지, 미치광이 취급받을바엔."
		), ColorUtils.chat("&7&l마을주민8"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.VILLAGER8);
	}
}
