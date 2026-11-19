package org.lazberry.xmaslegacy.infoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.List;

public class RoleSelectNpc extends AbstractNpc {

	public RoleSelectNpc() {
		super(List.of(
				"반가워요.",
				"저를 찾아오셨다는건 직업을 찾고 계시나보군요?",
				"이 세계에서 직업을 가진다는건 참 중요한 일이죠.",
				"어떤걸 원하시나요? 평화로운 농사나 광질? 혹은 전투적인걸 좋아하시나요?",
				"옆의 직업서를 통하여 원하는 직업을 선택해보세요.",
				"한번 정한 직업은 바꾸기 쉽지 않습니다. 신중히 선택하세요!"
		), ColorUtils.chat("&6&l동사무원"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.SELECT);
	}
}
