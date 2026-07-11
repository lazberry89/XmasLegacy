package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Wizard;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.SkillSet;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.WIZARD;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.ARCHMAGE_ZONE;

public class ArchmageZone implements Skills<Wizard.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Wizard.Container container) {
        return false;
    }

    @Override
    public @NotNull SkillSet type() {
        return ARCHMAGE_ZONE;
    }

    @Override
    public @NotNull Role role() {
        return WIZARD;
    }
}
