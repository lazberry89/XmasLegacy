package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant.Skill;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant.Merchant;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.SELL_ITEMS)
@Registry.Exclude(type = ServerType.LOBBY)
public class SellItems implements Skills<Merchant.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Merchant.@NotNull Container container) {
		container.priceManager().setOwner(caster.getUniqueId());
		caster.openInventory(container.priceManager().MerchantShop());
		caster.playSound(caster, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
		return false;
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.SELL_ITEMS;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.MERCHANT;
	}
}
