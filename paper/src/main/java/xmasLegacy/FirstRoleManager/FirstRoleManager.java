package xmasLegacy.FirstRoleManager;

import org.jetbrains.annotations.Contract;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.Farmer.Farmer;
import xmasLegacy.FirstRoleManager.Gatherer.Gatherer;
import xmasLegacy.FirstRoleManager.Merchant.Merchant;
import xmasLegacy.FirstRoleManager.Miner.Miner;
import xmasLegacy.FirstRoleManager.Priest.Priest;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class FirstRoleManager {
    private final Map<Roles, AbstractFirstRole> roleInstance = new HashMap<>();
    private AbstractFirstRole warrior;
    private AbstractFirstRole rogue;
    private AbstractFirstRole mage;
    private AbstractFirstRole knight;
    private AbstractFirstRole archer;
    private AbstractFirstRole priest;
    private AbstractFirstRole miner;
    private AbstractFirstRole merchant;
    private AbstractFirstRole gatherer;
    private AbstractFirstRole farmer;
    private AbstractFirstRole crafter;
    private static FirstRoleManager instance;

    public static FirstRoleManager getInstance() {
        if (instance == null) instance = new FirstRoleManager();
        return instance;
    }

    private FirstRoleManager() {}

	public void init() {
		this.warrior = Warrior.getInstance();
		this.rogue = Rogue.getInstance();
		this.mage = Mage.getInstance();
		this.knight = Knight.getInstance();
		this.archer = Archer.getInstance();
		this.priest = Priest.getInstance();
		this.miner = Miner.getInstance();
		this.merchant = Merchant.getInstance();
		this.gatherer = Gatherer.getInstance();
		this.farmer = Farmer.getInstance();
		this.crafter = Crafter.getInstance();
		this.roleInstance.put(Roles.WARRIOR, this.warrior);
		this.roleInstance.put(Roles.ROGUE, this.rogue);
		this.roleInstance.put(Roles.MAGE, this.mage);
		this.roleInstance.put(Roles.KNIGHT, this.knight);
		this.roleInstance.put(Roles.ARCHER, this.archer);
		this.roleInstance.put(Roles.PRIEST, this.priest);
		this.roleInstance.put(Roles.MINER, this.miner);
		this.roleInstance.put(Roles.MERCHANT, this.merchant);
		this.roleInstance.put(Roles.GATHERER, this.gatherer);
		this.roleInstance.put(Roles.FARMER, this.farmer);
		this.roleInstance.put(Roles.CRAFTER, this.crafter);
	}

    @Contract(value = "null -> null", pure = true)
    public AbstractFirstRole getRoleInstance(Roles role) {
        return this.roleInstance.get(role);
    }
}
