package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Wizard;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;

@Roles
public class Wizard extends AbstractSecondRole {
    private final @NotNull Container container;

    public record Container(
            XmasLegacy plugin
    ) implements RoleContainer {}

    public Wizard() {
        super(SecondaryRoles.WIZARD);
        this.container = new Container(getPlugin());
    }

    @Override
    public void useFirstSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.TARGET, getSkillManager().get(SecondarySkillSet.MANA_ORB), container, 30);
    }

    @Override
    public void useSecondSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.RANGE, getSkillManager().get(SecondarySkillSet.GLACIAL_PRISON), container, 30);
    }

    @Override
    public void usePassive(@NotNull Player p) {}

    @Override
    public @NotNull ItemStack roleWeapon() {
        return ItemBuilder.of(getPlugin(), Material.DIAMOND_AXE)
                .setRoleDefault(getRole())
                .setName(ColorUtils.chat("&b&l날카로운 도끼"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .hideAllFlags()
                .build().clone();
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.DIAMOND_CHESTPLATE)
                .setRoleDefault(getRole())
                .setName(ColorUtils.chat("&b&l단단한 갑옷"))
                .hideAllFlags()
                .build().clone();
    }
}
