package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public enum Roles implements Role {
    KNIGHT("기사", SecondaryRoles.GUARDIAN, SecondaryRoles.DEFENDER), //기사 o
    WARRIOR("전사", SecondaryRoles.BERSERKER, SecondaryRoles.FIGHTER), //전사 o
    ARCHER("궁수", SecondaryRoles.SNIPER, SecondaryRoles.RANGER, SecondaryRoles.TRAPPER), //궁수 o
    MAGE("마법사", SecondaryRoles.WIZARD, SecondaryRoles.ELEMENTAL, SecondaryRoles.SUMMONER), //마법사 o
    PRIEST("성직자", SecondaryRoles.MONK, SecondaryRoles.BISHOP), //성직자 o
    ROGUE("도적", SecondaryRoles.ASSASSIN, SecondaryRoles.REAPER), //도적 o
    CRAFTER("장인", SecondaryRoles.SMITH, SecondaryRoles.ALCHEMIST), //장인
	GATHERER("수집가"), //수집가 o
	FARMER("농부"), //농부 o
	MINER("광부"), //광부 o
	MERCHANT("상인"), //상인

	ADMIN("관리자"),
	USER("유저");

    private final String Prefix;
    private final List<Role> next;

    Roles(@NotNull String korName, @Nullable SecondaryRoles... next) {
        this.Prefix = korName;
        this.next = Arrays.asList(next);
    }

    @Override
    public @NotNull String getKor() {
        return this.Prefix;
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public @Nullable List<Role> next() {
        return this.next;
    }

    @Override
    public Role parent() {
        return null;
    }
}
