package xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class Villager9 extends AbstractNpc {
	public Villager9() {
		super(List.of(
				"아 안녕하세여?",
				"아 어디 가는건 아니고 그냥 도망치고있어요.",
				"아니 아빠가 자꾸 이상한소리 하잖아요.",
				"자꾸 결사대에 들어가래요. 전 그냥 농사나 하면서 살고싶은데."
		), ColorUtils.chat("&7&l마을주민9"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.VILLAGER9);
	}
}
