package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Trapper;

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

@Roles
public class Trapper extends AbstractSecondRole {
    private final @NotNull Container container;

    public record Container(
            XmasLegacy plugin
    ) implements RoleContainer {}

    public Trapper() {
        super(SecondaryRoles.TRAPPER);
        this.container = new Container(getPlugin());
    }

    @Override
    public void useFirstSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.TARGET, SkillManager.INSTANCE.get(SecondarySkillSet.SHOTGUN), container, 30);
    }

    @Override
    public void useSecondSkill(@NotNull Player p) {
        handleSkill(p, emblem, EmblemType.RANGE, SkillManager.INSTANCE.get(SecondarySkillSet.CHAIN_GRAB), container, 30);
    }

    @Override
    public void usePassive(@NotNull Player p) {}

    @Override
    public @NotNull ItemStack roleWeapon() {
        return ItemBuilder.of(getPlugin(), Material.COPPER_AXE)
                .setRoleDefault(getRole())
                .setName(ColorUtils.chat("&6&l사냥꾼의 도끼"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .hideAllFlags()
                .build().clone();
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), Material.IRON_BOOTS)
                .setRoleDefault(getRole())
                .setName(ColorUtils.chat("&7&l낡은 장화"))
                .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
                .hideAllFlags()
                .build().clone();
    }
}
