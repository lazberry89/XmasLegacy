package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.BasicRoles;

import java.util.HashMap;
import java.util.Map;

public enum FirstRoleManager {
    INSTANCE;

    private final @NotNull Map<BasicRoles, AbstractFirstRole> roleInstance = new HashMap<>();

	FirstRoleManager() {}

	public void register(@NotNull AbstractFirstRole role) {
		roleInstance.put(role.getRole(), role);
	}

    @SuppressWarnings("unchecked")
    @Contract(value = "null -> null", pure = true)
    public <R extends AbstractFirstRole> @Nullable R getRoleInstance(@Nullable BasicRoles role) {
	    if (role == null) return null;
        return (R) this.roleInstance.get(role);
    }
}
