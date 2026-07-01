package org.lazberry.xmaslegacy;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.FirstRoleManager;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.SecondRoleManager;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;

@SuppressWarnings("unchecked")
public enum RoleManager {
	INSTANCE;

	private final @NotNull FirstRoleManager frm;
	private final @NotNull SecondRoleManager srm;

    RoleManager() {
		this.frm = FirstRoleManager.INSTANCE;
		this.srm = SecondRoleManager.INSTANCE;
    }

	public @NotNull <F extends AbstractFirstRole> F getRoleInstance(@NotNull BasicRoles role) throws IllegalArgumentException {
		var result = frm.getRoleInstance(role);
		if (result == null) throw new IllegalArgumentException("No role found");
		return (F) result;
	}

	public @NotNull <S extends AbstractSecondRole> S getRoleInstance(@NotNull SecondaryRoles role) throws IllegalArgumentException {
		var result = srm.getRoleInstance(role);
		if (result == null) throw new IllegalArgumentException("No role found");
		return (S) result;
	}

	public @NotNull <R extends UsingEnergy> R getRoleInstance(@NotNull Role role) throws IllegalArgumentException {
		return (R) switch (role) {
			case BasicRoles b -> getRoleInstance(b);
			case SecondaryRoles s -> getRoleInstance(s);
			default -> throw new IllegalArgumentException("지원하지 않는 직업 타입입니다: " + role.getClass().getName());
		};
	}
}
