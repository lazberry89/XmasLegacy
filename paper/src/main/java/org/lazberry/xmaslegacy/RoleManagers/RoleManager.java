package org.lazberry.xmaslegacy.RoleManagers;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.HiddenRoleManager.AbstractHiddenRole;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.RoleManagers.ThirdRoleManager.AbstractThirdRole;
import org.lazberry.xmaslegacy.Roles.*;

import java.util.EnumMap;
import java.util.Map;

public enum RoleManager {
	INSTANCE;

	private final @NotNull Map<BasicRoles, AbstractFirstRole> firstRoleMap = new EnumMap<>(BasicRoles.class);
	private final @NotNull Map<SecondaryRoles, AbstractSecondRole> secondRoleMap = new EnumMap<>(SecondaryRoles.class);
	private final @NotNull Map<ThirdRoles, AbstractThirdRole> thirdRoleMap = new EnumMap<>(ThirdRoles.class);
	private final @NotNull Map<HiddenRoles, AbstractHiddenRole> hiddenRoleMap = new EnumMap<>(HiddenRoles.class);

    RoleManager() {}

	public void register(@NotNull RoleClass instance) {
		switch (instance) {
			case AbstractFirstRole afr -> this.firstRoleMap.put(afr.getRole(), afr);
			case AbstractSecondRole asr -> this.secondRoleMap.put(asr.getRole(), asr);
			case AbstractThirdRole atr -> this.thirdRoleMap.put(atr.getRole(), atr);
			case AbstractHiddenRole ahr -> this.hiddenRoleMap.put(ahr.getRole(), ahr);
            default -> throw new IllegalStateException("Unexpected value: " + instance);
        }
	}

	@SuppressWarnings("unchecked")
	public <V extends RoleClass> @NotNull V getRoleInstance(@NotNull Role role) {
		return (V) switch (role) {
			case BasicRoles basic -> this.firstRoleMap.get(basic);
			case SecondaryRoles second -> this.secondRoleMap.get(second);
			case ThirdRoles third -> this.thirdRoleMap.get(third);
			case HiddenRoles hidden -> this.hiddenRoleMap.get(hidden);
			default -> throw new IllegalArgumentException("Unexpected value: " + role);
		};
	}

	public @NotNull AbstractFirstRole getBasicInstance(@NotNull BasicRoles role) {
		return this.firstRoleMap.get(role);
	}
	public @NotNull AbstractSecondRole getSecondInstance(@NotNull SecondaryRoles role) {
		return this.secondRoleMap.get(role);
	}
	public @NotNull AbstractThirdRole getThirdInstance(@NotNull ThirdRoles role) {
		return this.thirdRoleMap.get(role);
	}
	public @NotNull AbstractHiddenRole getHiddenInstance(@NotNull HiddenRoles role) {
		return this.hiddenRoleMap.get(role);
	}
}
