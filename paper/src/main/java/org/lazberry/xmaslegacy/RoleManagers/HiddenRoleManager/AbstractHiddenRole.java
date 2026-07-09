package org.lazberry.xmaslegacy.RoleManagers.HiddenRoleManager;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.RoleClass;
import org.lazberry.xmaslegacy.Roles.HiddenRoles;

public abstract class AbstractHiddenRole implements RoleClass {
    private final @NotNull @Getter HiddenRoles role;

    public AbstractHiddenRole(@NotNull HiddenRoles role) {
        this.role = role;
    }
}
