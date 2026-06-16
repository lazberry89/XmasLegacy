package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.Skill;

import java.util.Arrays;
import java.util.List;

public enum BasicRoles implements Role {
    KNIGHT("기사", BasicSkills.SHARP_SWEEPING, BasicSkills.TAUNT, 5, SecondaryRoles.GUARDIAN, SecondaryRoles.DEFENDER), //기사 o
    WARRIOR("전사", BasicSkills.TOMAHAWK, BasicSkills.BLOOD_FRENZY, 5, SecondaryRoles.BERSERKER, SecondaryRoles.FIGHTER), //전사 o
    ARCHER("궁수", BasicSkills.SHOCK_DART, BasicSkills.BACK_DASH, 5, SecondaryRoles.SNIPER, SecondaryRoles.RANGER, SecondaryRoles.TRAPPER), //궁수 o
    MAGE("마법사", BasicSkills.COMPACT_INSANELY, BasicSkills.GRAVITY, 5, SecondaryRoles.WIZARD, SecondaryRoles.ELEMENTAL, SecondaryRoles.SUMMONER), //마법사 o
    PRIEST("성직자", BasicSkills.COMPACT_HEAL, BasicSkills.STEROID, 5, SecondaryRoles.MONK, SecondaryRoles.BISHOP), //성직자 o
    ROGUE("도적", BasicSkills.DAGGER_RUSH, BasicSkills.SMOKE, 5, SecondaryRoles.ASSASSIN, SecondaryRoles.REAPER), //도적 o
    CRAFTER("장인", BasicSkills.FIX, BasicSkills.TEMP_BUFF, 5, SecondaryRoles.SMITH, SecondaryRoles.ALCHEMIST), //장인
	GATHERER("수집가", BasicSkills.ETERNAL_POSE, BasicSkills.TRUTH_EYE, 5), //수집가 o
	FARMER("농부", BasicSkills.SHOCK_DART, BasicSkills.BACK_DASH, 5), //농부 o
	MINER("광부", BasicSkills.CHAIN_MINING, BasicSkills.ORE_EYE, 5), //광부 o
	MERCHANT("상인", BasicSkills.OPEN_STOCKS, BasicSkills.SELL_ITEMS, 5), //상인

	ADMIN("관리자", null, null, 5),
	USER("유저", null, null, 0);

    private final String Prefix;
	private final BasicSkills skill1;
	private final BasicSkills skill2;
    private final int dashCount;
    private final List<Role> next;

    BasicRoles(@NotNull String korName, BasicSkills skill1, BasicSkills skill2, int dashCount, @Nullable SecondaryRoles... next) {
        this.Prefix = korName;
		this.skill1 = skill1;
		this.skill2 = skill2;
        this.dashCount = dashCount;
        this.next = Arrays.asList(next);
    }

    public BasicSkills getFirstSkill() {
		return this.skill1;
	}

	public BasicSkills getSecondSkill() {
		return this.skill2;
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
    public @NotNull List<Role> next() {
        return this.next;
    }

    @Override
    public Skill bindTarget() {
        return this.getFirstSkill();
    }

    @Override
    public List<Skill> bindRange() {
        return List.of(this.getSecondSkill());
    }

    @Override
    public int getDashCount() {
        return this.dashCount;
    }

    @Override
    public Role parent() {
        return null;
    }
}
