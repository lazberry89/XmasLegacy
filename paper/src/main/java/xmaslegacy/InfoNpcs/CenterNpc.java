package xmaslegacy.InfoNpcs;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class CenterNpc extends AbstractNpc {

	public CenterNpc() {
		super(List.of(
				"크리아 마을에 온 이상, 가만히 놀고만 있지는 못할거야.",
				"마을 여기저기 푸른 빛 불이 보이는가?",
				"그 불이 바로 우리 선조가 남겨둔 이 세상 마지막 &b불꽃&f일세.",
				"이 마을을 넘어서면 더 이상 살수도, 존재할 수 조차 없어지지.",
				"근데 불이 점점 죽고있어.",
				"직업을 찾게. 직업을 찾고, 성장하게.",
				"서로 도와야만 마을 밖을 나설 수 있고, 나가서 &4\"그것\"&f을 없애야돼.",
				"그것이 뭐냐고?",
				"...",
				"저기 &6도서관&f에 가면 찾아볼 수 있을거야. 여기서 말하긴 꺼려지는군.",
				"옆에 서적이 보이는가? 저 책이 자네의 직업을 결정해 줄걸세.",
				"행운을 비네. 서로 도와야만 살아남을 수 있어."
		), ColorUtils.chat("&b&l마을 이장"));
	}

	@Override
	protected @NotNull String next(@NotNull Player player) {
		var uuid = player.getUniqueId();
		int num = this.playerCaption.getOrDefault(uuid, 0);
		String currentCaption = this.caption.get(num);

		num++;

		if (num >= this.caption.size()) num = 0;

		this.playerCaption.put(uuid, num);
		return currentCaption;
	}
}
