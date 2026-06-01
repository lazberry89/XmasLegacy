package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.SecondarySkill;
import org.lazberry.xmaslegacy.settings.Skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SecondaryRoles implements Role {
	GUARDIAN("가디언" ,Roles.KNIGHT, SecondarySkill.TARGET_GUARD, new Skill[]{SecondarySkill.OVERCHARGE_PRISM}, ThirdRoles.PALADIN), //기사 2차전직 가디언
	DEFENDER("디펜더" ,Roles.KNIGHT, SecondarySkill.MAGNETIC_FIELD, new Skill[]{SecondarySkill.KARMA}, ThirdRoles.PALADIN), //기사 2차전직 디펜더

	BERSERKER("버서커" ,Roles.WARRIOR, SecondarySkill.MADNESS, new Skill[]{SecondarySkill.TRIPLE_TOMAHAWK}), //전사 2차전직 버서커(탱커)
	FIGHTER("격투가" ,Roles.WARRIOR, SecondarySkill.COUNTER, new Skill[]{SecondarySkill.FINISHER}), //전사 2차전직 격투가

	SNIPER("저격수" ,Roles.ARCHER, SecondarySkill.SNIPE, new Skill[]{SecondarySkill.MAGIC_BULLET}, ThirdRoles.WIND_WALKER), //궁수 2차전직 저격수
	RANGER("유격병" ,Roles.ARCHER, ThirdRoles.WIND_WALKER), //궁수 2차전직 유격병
	TRAPPER("사냥꾼" ,Roles.ARCHER, ThirdRoles.WIND_WALKER), //궁수 2차전직 사냥꾼

	WIZARD("위자드" ,Roles.MAGE, ThirdRoles.ARCHMAGE), //마법사 2차전직 마법사(스킬변화)
	ELEMENTAL("엘리멘탈" ,Roles.MAGE, ThirdRoles.ARCHMAGE), //마법사 2차전직 엘리멘탈(원소 마법)
	SUMMONER("소환수" ,Roles.MAGE, ThirdRoles.ARCHMAGE), //마법사 2차전직 소환수

	BISHOP("주교" ,Roles.PRIEST, ThirdRoles.SAINT), //성직자 2차전직 주교
	MONK("수도사" ,Roles.PRIEST, ThirdRoles.SAINT), //성직자 2차전직 수도사

	ASSASSIN("어쌔신" ,Roles.ROGUE, ThirdRoles.SHADOW_MASTER), //도적 2차전직 어쌔신
	REAPER("리퍼" ,Roles.ROGUE, ThirdRoles.SHADOW_MASTER), //도적 2차전직 리퍼

	SMITH("대장장이" ,Roles.CRAFTER), //장인 2차전직 대장장이
	ALCHEMIST("연금술사" ,Roles.CRAFTER); //장인 2차전직 연금술사

	private final Roles parent;
	private final List<Role> next;
	private final String kor;
	private final Skill skill1;
	private final List<Skill> skill2;

	SecondaryRoles(String kor, @NotNull Roles parent, @Nullable Role... next) {
		this.parent = parent;
		this.next = Arrays.asList(next);
		this.kor = kor;
		this.skill1 = null;
		this.skill2 = new ArrayList<>();
	}

	SecondaryRoles(String kor, @NotNull Roles parent, Skill skill1, Skill[] skill2, @Nullable Role... next) {
		this.parent = parent;
		this.next = Arrays.asList(next);
		this.kor = kor;
		this.skill1 = skill1;
		this.skill2 = List.of(skill2);
	}

	@Override
	public @Nullable Role parent() {
		return this.parent;
	}
	public @NotNull List<Role> next() {
		return this.next;
	}

	@Override
	public Skill bindTarget() {
		return this.skill1;
	}

	@Override
	public List<Skill> bindRange() {
		return this.skill2;
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
