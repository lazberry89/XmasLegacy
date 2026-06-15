package xmaslegacy;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import xmaslegacy.RoleManagers.FirstRoleManager.FirstRoleManager;
import xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import xmaslegacy.RoleManagers.SecondaryRoleManager.SecondRoleManager;

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
		this.frm = FirstRoleManager.INSTANCE;
		this.srm = SecondRoleManager.INSTANCE;
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
