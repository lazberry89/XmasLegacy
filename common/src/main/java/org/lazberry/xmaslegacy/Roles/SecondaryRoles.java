package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SecondaryRoles implements Role {
	GUARDIAN("가디언" , BasicRoles.KNIGHT, SecondarySkillSet.TARGET_GUARD, new SkillSet[]{SecondarySkillSet.OVERCHARGE_PRISM}, ThirdRoles.PALADIN), //기사 2차전직 가디언
	DEFENDER("디펜더" , BasicRoles.KNIGHT, SecondarySkillSet.MAGNETIC_FIELD, new SkillSet[]{SecondarySkillSet.KARMA}, ThirdRoles.PALADIN), //기사 2차전직 디펜더

	BERSERKER("버서커" , BasicRoles.WARRIOR, SecondarySkillSet.MADNESS, new SkillSet[]{SecondarySkillSet.TRIPLE_TOMAHAWK}), //전사 2차전직 버서커(탱커)
	FIGHTER("격투가" , BasicRoles.WARRIOR, SecondarySkillSet.COUNTER, new SkillSet[]{SecondarySkillSet.FINISHER}), //전사 2차전직 격투가

	SNIPER("저격수" , BasicRoles.ARCHER, SecondarySkillSet.SNIPE, new SkillSet[]{SecondarySkillSet.MAGIC_BULLET}, ThirdRoles.WIND_WALKER), //궁수 2차전직 저격수
	RANGER("유격병" , BasicRoles.ARCHER, SecondarySkillSet.PRISM_LASER, new SkillSet[]{SecondarySkillSet.CHAINING}, ThirdRoles.WIND_WALKER), //궁수 2차전직 유격병
	TRAPPER("사냥꾼" , BasicRoles.ARCHER, ThirdRoles.WIND_WALKER), //궁수 2차전직 사냥꾼

	WIZARD("위자드" , BasicRoles.MAGE, ThirdRoles.ARCHMAGE), //마법사 2차전직 마법사(스킬변화)
	ELEMENTAL("엘리멘탈" , BasicRoles.MAGE, ThirdRoles.ARCHMAGE), //마법사 2차전직 엘리멘탈(원소 마법)
	SUMMONER("소환수" , BasicRoles.MAGE, ThirdRoles.ARCHMAGE), //마법사 2차전직 소환수

	BISHOP("주교" , BasicRoles.PRIEST, ThirdRoles.SAINT), //성직자 2차전직 주교
	MONK("수도사" , BasicRoles.PRIEST, ThirdRoles.SAINT), //성직자 2차전직 수도사

	ASSASSIN("어쌔신" , BasicRoles.ROGUE, ThirdRoles.SHADOW_MASTER), //도적 2차전직 어쌔신
	REAPER("리퍼" , BasicRoles.ROGUE, ThirdRoles.SHADOW_MASTER), //도적 2차전직 리퍼

	SMITH("대장장이" , BasicRoles.CRAFTER), //장인 2차전직 대장장이
	ALCHEMIST("연금술사" , BasicRoles.CRAFTER); //장인 2차전직 연금술사

	private final BasicRoles parent;
	private final List<Role> next;
	private final String kor;
	private final SkillSet skillSet1;
	private final List<SkillSet> skillSet2;

	SecondaryRoles(String kor, @NotNull BasicRoles parent, @Nullable Role... next) {
		this.parent = parent;
		this.next = Arrays.asList(next);
		this.kor = kor;
		this.skillSet1 = null;
		this.skillSet2 = new ArrayList<>();
	}

	SecondaryRoles(String kor, @NotNull BasicRoles parent, SkillSet skillSet1, SkillSet[] skillSet2, @Nullable Role... next) {
		this.parent = parent;
		this.next = Arrays.asList(next);
		this.kor = kor;
		this.skillSet1 = skillSet1;
		this.skillSet2 = List.of(skillSet2);
	}

	@Override
	public @Nullable Role parent() {
		return this.parent;
	}
	public @NotNull List<Role> next() {
		return this.next;
	}

	@Override
	public SkillSet bindTarget() {
		return this.skillSet1;
	}

	@Override
	public List<SkillSet> bindRange() {
		return this.skillSet2;
	}

	@Override
	public int getDashCount() {
		return 0;
	}

	@Override
	public String getKor() {
		return this.kor;
	}

	@Override
	public int getTier() {return 2;}
}
