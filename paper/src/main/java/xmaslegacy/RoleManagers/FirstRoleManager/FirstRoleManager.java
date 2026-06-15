package xmaslegacy.RoleManagers.FirstRoleManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.Roles;

import java.util.HashMap;
import java.util.Map;

public enum FirstRoleManager {
    INSTANCE;

    private final @NotNull Map<Roles, AbstractFirstRole> roleInstance = new HashMap<>();

	FirstRoleManager() {}

	public void register(@NotNull AbstractFirstRole role) {
		roleInstance.put(role.getRole(), role);
	}

    @Contract(value = "null -> null", pure = true)
    @SuppressWarnings("unchecked")
    public <R extends AbstractFirstRole> @Nullable R getRoleInstance(@Nullable Roles role) {
	    if (role == null) return null;
        return (R) this.roleInstance.get(role);
    }
}
