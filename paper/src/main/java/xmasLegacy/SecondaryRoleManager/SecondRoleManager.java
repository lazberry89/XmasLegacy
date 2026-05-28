package xmasLegacy.SecondaryRoleManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import xmasLegacy.SecondaryRoleManager.Sniper.Sniper;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class SecondRoleManager {
	private Berserker berserker;
	private Defender defender;
	private Guardian guardian;
	private Fighter fighter;
	private Sniper sniper;
	private final Map<SecondaryRoles, AbstractSecondRole> roleInstance = new HashMap<>();
	private static SecondRoleManager instance;

	public static SecondRoleManager getInstance() {
		if (instance == null) instance = new SecondRoleManager();
		return instance;
	}

	private SecondRoleManager() {}

	public void init() {
		this.berserker = Berserker.getInstance();
		this.defender = Defender.getInstance();
		this.guardian = Guardian.getInstance();
		this.fighter = Fighter.getInstance();
		this.sniper = Sniper.getInstance();
		this.roleInstance.put(SecondaryRoles.BERSERKER, this.berserker);
		this.roleInstance.put(SecondaryRoles.DEFENDER, this.defender);
		this.roleInstance.put(SecondaryRoles.GUARDIAN, this.guardian);
		this.roleInstance.put(SecondaryRoles.FIGHTER, this.fighter);
		this.roleInstance.put(SecondaryRoles.SNIPER, this.sniper);
	}

	public AbstractSecondRole getRoleInstance(@NotNull SecondaryRoles role) {
		return this.roleInstance.get(role);
	}

	public static @Nullable SecondaryRoles getRole(String str) {
		try {
			return SecondaryRoles.valueOf(str.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
