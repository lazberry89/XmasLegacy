package xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class Villager1 extends AbstractNpc {
	public Villager1() {
		super(List.of(
				"자네 그거 들었나? 마을 오른쪽 아래 탑에 이상한 마녀가 산다는군.",
				"웬만해선 가지마. 이상한 소리를 계속 하고 있다던데?",
				"지금 우리도 죽기 직전인데, 결사대를 막으라니?",
				"결사대가 우리를 위해 지금 얼마나 노력하는데.",
				"그냥 미친 할멈이야.",
				"그래도 갈거면 가서 뭐라는지나 들어봐. 이상한 사람일세."
		), ColorUtils.chat("&7&l마을주민1"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.VILLAGER1);
	}
}
