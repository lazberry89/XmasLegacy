package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.Skill;

import java.util.List;

public enum ThirdRoles implements Role {
	PALADIN("팔라딘" , BasicRoles.KNIGHT), //기사 3차전직
	WIND_WALKER("윈드워커" , BasicRoles.ARCHER), //궁수 3차전직
	ARCHMAGE("아크메이지" , BasicRoles.MAGE), //마법사 3차전직
	SAINT("세인트" , BasicRoles.PRIEST), //성직자 3차전직
	SHADOW_MASTER("Mr.셰도우" , BasicRoles.ROGUE); //도적 3차전직

	private final BasicRoles parent;
	private final String kor;

	ThirdRoles(String kor , BasicRoles parent) {
		this.kor = kor;
		this.parent = parent;
	}
	@Override
	public @Nullable Role parent() {return this.parent;}

	@Override
	public String getKor() {return this.kor;}

	@Override
	public int getTier() {return 3;}
	@Override
	public @NotNull List<Role> next() {return List.of();}

	@Override
	public Skill bindTarget() {
		return null;
	}

	@Override
	public List<Skill> bindRange() {
		return List.of();
	}

	@Override
	public int getDashCount() {
		return 0;
	}
}
