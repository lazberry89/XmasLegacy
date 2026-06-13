package xmasLegacy.FirstRoleManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.Farmer.Farmer;
import xmasLegacy.FirstRoleManager.Gatherer.Gatherer;
import xmasLegacy.FirstRoleManager.Merchant.Merchant;
import xmasLegacy.FirstRoleManager.Miner.Miner;
import xmasLegacy.FirstRoleManager.Priest.Priest;

import java.util.HashMap;
import java.util.Map;

public enum FirstRoleManager {
    INSTANCE;

    private final @NotNull Map<Roles, AbstractFirstRole> roleInstance = new HashMap<>();

	FirstRoleManager() {}

	public void init() {
		this.roleInstance.put(Roles.WARRIOR, new Warrior());
		this.roleInstance.put(Roles.ROGUE, new Rogue());
		this.roleInstance.put(Roles.MAGE, new Mage());
		this.roleInstance.put(Roles.KNIGHT, new Knight());
		this.roleInstance.put(Roles.ARCHER, new Archer());
		this.roleInstance.put(Roles.PRIEST, new Priest());
		this.roleInstance.put(Roles.MINER, new Miner());
		this.roleInstance.put(Roles.MERCHANT, new Merchant());
		this.roleInstance.put(Roles.GATHERER, new Gatherer());
		this.roleInstance.put(Roles.FARMER, new Farmer());
		this.roleInstance.put(Roles.CRAFTER, new Crafter());
	}

    @Contract(value = "null -> null", pure = true)
    @SuppressWarnings("unchecked")
    public <R extends AbstractFirstRole> @Nullable R getRoleInstance(@Nullable Roles role) {
	    if (role == null) return null;
        return (R) this.roleInstance.get(role);
    }
}
