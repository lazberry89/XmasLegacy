package xmasLegacy.SecondaryRoleManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import xmasLegacy.SecondaryRoleManager.Sniper.Sniper;

import java.util.HashMap;
import java.util.Map;

public enum SecondRoleManager {
	INSTANCE;

	private final @NotNull Map<SecondaryRoles, AbstractSecondRole> roleInstance = new HashMap<>();

	SecondRoleManager() {}

	public void init() {
		this.roleInstance.put(SecondaryRoles.BERSERKER, new Berserker());
		this.roleInstance.put(SecondaryRoles.DEFENDER, new Defender());
		this.roleInstance.put(SecondaryRoles.GUARDIAN, new Guardian());
		this.roleInstance.put(SecondaryRoles.FIGHTER, new Fighter());
		this.roleInstance.put(SecondaryRoles.SNIPER, new Sniper());
	}

	@Contract(value = "null -> null", pure = true)
	@SuppressWarnings("unchecked")
	public <T extends AbstractSecondRole> @Nullable T getRoleInstance(@Nullable SecondaryRoles role) {
		if (role == null) return null;
		return (T) this.roleInstance.get(role);
	}
}
