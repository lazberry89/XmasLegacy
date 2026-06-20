package xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class WanderingGrandpaNpc extends AbstractNpc {

	public WanderingGrandpaNpc() {
		super(List.of(
				"아 그 할멈? 허허..",
				"이상한소리를 했다고? 다들 그렇게 생각하더라.",
				"자네는 무엇을 위해 여기 왔는가?",
				"악당을 처치하러?",
				"악당이라..엘리안이 보고싶군.",
				"누구보다 우리를 위했지만, 동시에 그 누구보다 악당을 잘 알았던.",
				"그게 누구냐고? 그것도 모르면서 여기를 온거야?",
				"..."
		), ColorUtils.chat("&7&l떠돌이"), Sound.ENTITY_WANDERING_TRADER_AMBIENT);
	}
}
