package org.lazberry.xmaslegacy.RoleManagers.ThirdRoleManager;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.RoleClass;
import org.lazberry.xmaslegacy.Roles.ThirdRoles;

public abstract class AbstractThirdRole implements RoleClass {
    private final @NotNull @Getter ThirdRoles role;

    public AbstractThirdRole(@NotNull ThirdRoles role) {
        this.role = role;
    }
}
