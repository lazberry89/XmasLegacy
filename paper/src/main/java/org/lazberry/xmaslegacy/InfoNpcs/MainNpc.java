package org.lazberry.xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

@DefaultQualifier(NotNull.class)
public class MainNpc extends AbstractNpc {
    public MainNpc() {
        super(List.of(
                "반갑구만. 여기 마을은 처음인가?",
                "그전에, 여기 오래있다간 얼어죽을거야. 이 음식을 들고가게.",
                "그 음식은 앞으로 상점에서 구매할 수 있을거야. 물론 농부가 일을 잘해준다면 말이지.",
                "이 앞으로 길을 따라가면 당신의 직업을 정해볼 수 있을거야.",
                "진지하게 선택하게. 전직말고는 한번 직업은 평생 직업이기에.",
                "마을 중앙 신전에 도착하면 거기서 누군가 도와줄거야.",
		        "어서 가게. 시간이 없어."
		        ), ColorUtils.chat("&6&l크리아 마을 주민"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.MAIN);
    }
}
