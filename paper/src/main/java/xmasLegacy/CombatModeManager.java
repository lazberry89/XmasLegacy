package xmasLegacy;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused, FieldCanBeLocal")
public class CombatModeManager {
	private final UserManager um;
	private final Set<User> combatUsers = new HashSet<>();

	private static CombatModeManager instance;

	public static CombatModeManager getInstance() {
		if (instance == null) instance = new CombatModeManager();
		return instance;
	}

	private CombatModeManager() {
		this.um = UserManager.getInstance();
	}

	public void CombatMode(User u, boolean value) {
		u.setCombatMode(value);
		if (value) this.combatUsers.add(u);
		else  this.combatUsers.remove(u);
	}
	public boolean CombatMode(User u) {
		return u.getCombatValue();
	}
	public @NotNull Set<User> getCombatUsers() {
		return this.combatUsers;
	}
}
