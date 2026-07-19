package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Fighter;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Roles.Unpromotable;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;

@Roles(grade = 2)
public class Fighter extends AbstractSecondRole implements Unpromotable {
	private Container container;

	public record Container(
			XmasLegacy plugin
	) implements RoleContainer {}

    public Fighter() {
        super(SecondaryRoles.FIGHTER);
        this.container = new Container(getPlugin());
    }

    @Override
    public void useFirstSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.TARGET, SkillManager.INSTANCE.get(SecondarySkillSet.COUNTER), container, 30);
    }

    @Override
    public void useSecondSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.RANGE, SkillManager.INSTANCE.get(SecondarySkillSet.FINISHER), container, 30);
    }

    @Override
    public void useAdditional() {

    }

    @Override
    public void usePassive(@NotNull Player p) {}

    @Override
    public @NotNull ItemStack roleWeapon() {
        return ItemBuilder.of(getPlugin(), Material.IRON_HOE)
                .setName(ColorUtils.chat("&c&l복서의 글러브"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setRoleDefault(this.getRole())
                .build().clone();
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_HELMET)
                .setName(ColorUtils.chat("&7&l복서의 투구"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .setRoleDefault(this.getRole())
                .addAttribute(Attribute.ATTACK_SPEED, 0.01, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .build().clone();
    }
}
