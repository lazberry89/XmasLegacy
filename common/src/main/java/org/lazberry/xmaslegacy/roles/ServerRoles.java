package org.lazberry.xmaslegacy.roles;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ServerRoles implements Role {
    USER("유저", "기본 제공되는 역할입니다. 서버 구성원을 나타냅니다."),
    FARMER("농부", "농사를 할때 추가 경험치를 획득합니다.", "또한 작물을 판매시 추가금을 획득할 수 있습니다."),
    MINER("광부", "광물을 채집할때 추가 아이템과 경험치를 획득합니다.", "광물 판매시 추가금을 획득할 수 있습니다."),
    FISHERMAN("어부", "낚시를 할때 기본적인 버프가 적용됩니다.", "생선 판매시 추가금을 획득할 수 있습니다."),
    KNIGHT("기사", "몹을 사냥할 때 추가 아이템과 경험치를 획득합니다.", "타격시 확률적으로 치명타가 발생합니다."),
    BUILDER("건축가", "자재를 사용해서 건축시 일정량의 보상금과 경험치를 획득합니다.", "건축자재를 할인받으며, 건축 의뢰를 받을 수 있습니다."),
    FIGHTER("싸움꾼", "유저간 전투에서 승리금을 2배로 받습니다.", "패배시 일정 확률로 범칙금이 줄어듭니다."),
    BLACKSMITH("대장장이", "무기 강화를 의뢰받거나 직접 어디서든 할 수 있습니다.", "대장간 입장금이 면제됩니다.");

    ServerRoles(String name, String ... description) {
        this.name = name;
        this.description = String.join("\n", description);
        this.descriptionList = List.of(description);
    }

    private final String name;
    private final String description;
    private final List<String> descriptionList;

    @Override
    public @NotNull String getKor() {
        return name;
    }

    @Override
    public @NotNull String description() {
        return description;
    }

    public @NotNull List<String> getSortedDescription() {
        return descriptionList;
    }
}
