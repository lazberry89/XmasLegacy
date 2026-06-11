package xmasLegacy;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.FirstRoleManager.FirstRoleManager;
import xmasLegacy.SecondaryRoleManager.AbstractSecondRole;
import xmasLegacy.SecondaryRoleManager.SecondRoleManager;

public class RoleManager {
	private final FirstRoleManager frm;
	private final SecondRoleManager srm;
    private static volatile RoleManager instance;

	public static RoleManager getInstance() {
		if (instance == null) {
			synchronized (RoleManager.class) {
				if (instance == null) instance = new RoleManager();
			}
		}
		return instance;
	}

    private RoleManager() {
		this.frm = FirstRoleManager.getInstance();
		this.srm = SecondRoleManager.getInstance();
    }

	public @NotNull AbstractFirstRole getRoleInstance(@NotNull Roles role) {
		var result = frm.getRoleInstance(role);
		if (result == null) throw new NullPointerException("No role found");
		return result;
	}

	public @NotNull AbstractSecondRole getRoleInstance(@NotNull SecondaryRoles role) {
		var result = srm.getRoleInstance(role);
		if (result == null) throw new NullPointerException("No role found");
		return result;
	}
}
