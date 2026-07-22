package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Defender;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;

@Roles(grade = 2)
public class Defender extends AbstractSecondRole {
	private final Container container;

	public record Container(
			XmasLegacy plugin
	) implements RoleContainer {}

	public Defender() {
		super(SecondaryRoles.DEFENDER);
		this.container = new Container(getPlugin());
	}

	@Override
	public void useFirstSkill(@NotNull Player p) {
		handleSkill(p, emblem, EmblemType.TARGET, SkillManager.INSTANCE.get(SecondarySkillSet.SOUL_STEAL), container, 30);
	}

	@Override
	public void useSecondSkill(@NotNull Player p) {
	handleSkill(p, emblem, EmblemType.RANGE, SkillManager.INSTANCE.get(SecondarySkillSet.KARMA), container, 30);
	}

	@Override
	public void usePassive(@NotNull Player p) {}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.IRON_SWORD)
				.setName(ColorUtils.chat("&7&l단단한 철검"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), Material.IRON_CHESTPLATE)
				.setName(ColorUtils.chat("&7&l단단한 갑옷"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.build().clone();
	}
}
