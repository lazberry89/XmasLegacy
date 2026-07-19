package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant.Skill;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant.Merchant;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.OPEN_STOCKS)
public class OpenStocks implements Skills<Merchant.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Merchant.@NotNull Container container) {
		container.stockInterface().setOwner(caster);
		container.stockInterface().openStock(caster);
		caster.playSound(caster, Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1.0f, 1.0f);
		return false;
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.OPEN_STOCKS;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.MERCHANT;
	}
}
