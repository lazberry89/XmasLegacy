package org.lazberry.xmaslegacy.RoleManagers;

import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.HiddenRoleManager.AbstractHiddenRole;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.AbstractSecondRole;
import org.lazberry.xmaslegacy.RoleManagers.ThirdRoleManager.AbstractThirdRole;
import org.lazberry.xmaslegacy.Roles.*;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.EnumMap;
import java.util.Map;

@Registry.Exclude(type = ServerType.LOBBY)
public class RoleManager implements Initiator {
	private static final @NotNull Map<BasicRoles, AbstractFirstRole> firstRoleMap = new EnumMap<>(BasicRoles.class);
	private static final @NotNull Map<SecondaryRoles, AbstractSecondRole> secondRoleMap = new EnumMap<>(SecondaryRoles.class);
	private static final @NotNull Map<ThirdRoles, AbstractThirdRole> thirdRoleMap = new EnumMap<>(ThirdRoles.class);
	private static final @NotNull Map<HiddenRoles, AbstractHiddenRole> hiddenRoleMap = new EnumMap<>(HiddenRoles.class);

    public RoleManager() {}

	public static void register(@NotNull RoleClass instance) {
		switch (instance) {
			case AbstractFirstRole afr -> firstRoleMap.put(afr.getRole(), afr);
			case AbstractSecondRole asr -> secondRoleMap.put(asr.getRole(), asr);
			case AbstractThirdRole atr -> thirdRoleMap.put(atr.getRole(), atr);
			case AbstractHiddenRole ahr -> hiddenRoleMap.put(ahr.getRole(), ahr);
            default -> throw new IllegalStateException("Unexpected value: " + instance);
        }
	}

	@SuppressWarnings("unchecked")
	public <V extends RoleClass> @NotNull V getRoleInstance(@NotNull Role role) {
		return (V) switch (role) {
			case BasicRoles basic -> firstRoleMap.get(basic);
			case SecondaryRoles second -> secondRoleMap.get(second);
			case ThirdRoles third -> thirdRoleMap.get(third);
			case HiddenRoles hidden -> hiddenRoleMap.get(hidden);
			default -> throw new IllegalArgumentException("Unexpected value: " + role);
		};
	}

	public @NotNull AbstractFirstRole getBasicInstance(@NotNull BasicRoles role) {
		return firstRoleMap.get(role);
	}
	public @NotNull AbstractSecondRole getSecondInstance(@NotNull SecondaryRoles role) {
		return secondRoleMap.get(role);
	}
	public @NotNull AbstractThirdRole getThirdInstance(@NotNull ThirdRoles role) {
		return thirdRoleMap.get(role);
	}
	public @NotNull AbstractHiddenRole getHiddenInstance(@NotNull HiddenRoles role) {
		return hiddenRoleMap.get(role);
	}

	@Override
	public void init() {

	}
}
