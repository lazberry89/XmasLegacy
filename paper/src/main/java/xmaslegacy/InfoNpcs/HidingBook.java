package xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class HidingBook extends AbstractNpc {
	public HidingBook() {
		super(List.of(
				"도서관에 갔다왔냐고?",
				"..최근엔 간적 없어. 요즘 마을이 흉흉해서 집밖에도 잘 안나갔다고.",
				"책장은 왜..?",
				"왜 의심하는거야? 뭐 도둑맞은거라도 있어서 그래?",
				"...",
				"...",
				"...",
				"...",
				"...",
				"...",
				"...",
				"...",
				"...",
				"...",
				"...",
				"...",
				"...",
				"..무슨일이 있어도,",
				"그 서적은 세상에 알려져선 안돼.",
				"우리의 희망이 희망이 아니었단걸 알게 된 순간, 이 마을은 끝이야."
		), ColorUtils.chat("&8&l의심스런 주민"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.BOOK);
	}
}
