package xmasLegacy.FirstRoleManager;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class RoleManager {
    private final Map<Roles, AbstractFirstRole> roleInstance = new HashMap<>();
    private final XmasLegacy plugin = JavaPlugin.getPlugin(XmasLegacy.class);
    private final AbstractFirstRole warrior;
    private final AbstractFirstRole rogue;
    private final AbstractFirstRole mage;
    private final AbstractFirstRole knight;
    private final AbstractFirstRole archer;
    private final AbstractFirstRole priest;
    private final AbstractFirstRole miner;
    private final AbstractFirstRole merchant;
    private final AbstractFirstRole gatherer;
    private final AbstractFirstRole farmer;
    private final AbstractFirstRole crafter;

    public RoleManager() {
        this.warrior = plugin.warrior;
        this.rogue = plugin.rogue;
        this.mage = plugin.mage;
        this.knight = plugin.knight;
        this.archer = plugin.archer;
        this.priest = plugin.priest;
        this.miner = plugin.miner;
        this.merchant = plugin.merchant;
        this.gatherer = plugin.gatherer;
        this.farmer = plugin.farmer;
        this.crafter = plugin.crafter;
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
