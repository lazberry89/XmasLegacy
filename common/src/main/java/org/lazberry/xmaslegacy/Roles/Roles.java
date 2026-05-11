package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.NotNull;

public enum Roles {
    KNIGHT("기사"), //기사 o
    WARRIOR("전사"), //전사 o
    ARCHER("궁수"), //궁수 o
    MAGE("마법사"), //마법사 o
    PRIEST("성직자"), //성직자 o
    ROGUE("도적"), //도적 o
    CRAFTER("장인"), //장인
	GATHERER("수집가"), //수집가 o
	FARMER("농부"), //농부 o
	MINER("광부"), //광부 o
	MERCHANT("상인"), //상인

	ADMIN("관리자"),
	USER("유저");

    private final String Prefix;

    Roles(String korName) {
        this.Prefix = korName;
    }

    public @NotNull String getKor() {
        return this.Prefix;
    }
}
