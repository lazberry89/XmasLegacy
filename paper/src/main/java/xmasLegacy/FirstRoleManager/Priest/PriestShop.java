package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.Merchant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.EconomyManager;

import java.util.*;

public class PriestShop {
    private final ConductableItems CDI;
    private final EconomyManager EM;
	private final Map<InventoryHolder, Player> shopOwners = new HashMap<>();

    private boolean isShopEnabled = false;
    private int DragonStock = 0;
    private int HealerStock = 0;
    private int ProtectionStock = 0;
    private int SpearStock = 0;
    private int SaveStock = 0;

    public PriestShop(ConductableItems CDI,  EconomyManager EM) {
        this.CDI = CDI;
        this.EM = EM;
    }

    public void setDragonStock(int DragonStock) {this.DragonStock = DragonStock;}
    public void setHealerStock(int HealerStock) {this.HealerStock = HealerStock;}
    public void setProtectionStock(int ProtectionStock) {this.ProtectionStock = ProtectionStock;}
    public void setSpearStock(int SpearStock) {this.SpearStock = SpearStock;}
    public void setSaveStock(int SaveStock) {this.SaveStock = SaveStock;}

    public void addDragonStock(int DragonStock) {this.DragonStock += DragonStock;}
    public void addHealerStock(int HealerStock) {this.HealerStock += HealerStock;}
    public void addProtectionStock(int ProtectionStock) {this.ProtectionStock += ProtectionStock;}
    public void addSpearStock(int SpearStock) {this.SpearStock += SpearStock;}
    public void addSaveStock(int SaveStock) {this.SaveStock += SaveStock;}

    public int getDragonStock() {return this.DragonStock;}
    public int getHealerStock() {return this.HealerStock;}
    public int getProtectionStock() {return this.ProtectionStock;}
    public int getSpearStock() {return this.SpearStock;}
    public int getSaveStock() {return this.SaveStock;}
	public int getStockCount() {return DragonStock + HealerStock + ProtectionStock + SpearStock + SaveStock;}

    public void enableShop() {this.isShopEnabled = true;}
    public void disableShop() {this.isShopEnabled = false;}
    public boolean isShopEnabled() {return this.isShopEnabled;}

    public void openShop(Player viewer, Player owner) {
        if (!isShopEnabled()) return;
    }
    @Contract("_ -> _")
	public @Nullable Player getOwner(@Nullable Merchant merchant) {
		return shopOwners.getOrDefault(merchant, null);
	}

	public void removeShop(Merchant merchant) {
		shopOwners.remove(merchant);
	}
}
