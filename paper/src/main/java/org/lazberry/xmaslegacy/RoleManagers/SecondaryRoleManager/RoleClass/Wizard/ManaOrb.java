package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Wizard;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.WIZARD;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.MANA_ORB;

@Skill(type = PlayerSkills.MANA_ORB)
public class ManaOrb implements Skills<Wizard.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Wizard.Container container) {
        return false;
    }

    @Override
    public @NotNull SkillSet type() {
        return MANA_ORB;
    }

    @Override
    public @NotNull Role role() {
        return WIZARD;
    }
}
