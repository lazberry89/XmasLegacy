package xmaslegacy.RoleManagers.SecondaryRoleManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;

import java.util.HashMap;
import java.util.Map;

public enum SecondRoleManager {
	INSTANCE;

	private final @NotNull Map<SecondaryRoles, AbstractSecondRole> roleInstance = new HashMap<>();

	SecondRoleManager() {}

	public void register(@NotNull AbstractSecondRole role) {
		this.roleInstance.put(role.getRole(), role);
	}

	@Contract(value = "null -> null", pure = true)
	@SuppressWarnings("unchecked")
	public <T extends AbstractSecondRole> @Nullable T getRoleInstance(@Nullable SecondaryRoles role) {
		if (role == null) return null;
		return (T) this.roleInstance.get(role);
	}
}
