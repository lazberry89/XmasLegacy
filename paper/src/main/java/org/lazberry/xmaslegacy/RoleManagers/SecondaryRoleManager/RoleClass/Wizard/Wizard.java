package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Wizard;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Roles;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.XmasLegacy;

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

    }

    @Override
    public void useSecondSkill(@NotNull Player p) {

    }

    @Override
    public void usePassive(@NotNull Player p) {

    }

    @Override
    public @NotNull ItemStack roleWeapon() {
        return null;
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return null;
    }
}
