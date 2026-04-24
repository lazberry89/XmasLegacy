package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.EconomyManager;
import xmasLegacy.Economy.CurrencyManager;

import java.util.*;

public class PriestShop {
    private final ConductableItems CDI;
    private final EconomyManager EM;
	private final Map<Merchant, Player> shopOwners = new HashMap<>();
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

    public void enableShop() {this.isShopEnabled = true;}
    public void disableShop() {this.isShopEnabled = false;}
    public boolean isShopEnabled() {return this.isShopEnabled;}

    public void openShop(Player viewer, Player owner) {
        if (!isShopEnabled()) return;
        Merchant merchant = Bukkit.createMerchant();
		shopOwners.put(merchant, owner);

        List<MerchantRecipe> recipes = new ArrayList<>();
        MerchantRecipe recipe1 = new MerchantRecipe(CDI.DragonPotion(), this.DragonStock);
        recipe1.setSpecialPrice(EM.getPriceAdjustment("dragon_potion", Constants.DRAGON_BREATH_PRICE/100));
        recipe1.addIngredient(CurrencyManager.currency(Constants.DRAGON_BREATH_PRICE/100));
        recipes.add(recipe1);

        MerchantRecipe recipe2 = new MerchantRecipe(CDI.HealerPotion(), this.HealerStock);
        recipe2.setSpecialPrice(EM.getPriceAdjustment("healer_potion", Constants.HEALER_POTION_PRICE/100));
        recipe2.addIngredient(CurrencyManager.currency(Constants.HEALER_POTION_PRICE/100));
        recipes.add(recipe2);

		MerchantRecipe recipe3 = new MerchantRecipe(CDI.ProtectionPotion(), this.ProtectionStock);
        recipe3.setSpecialPrice(EM.getPriceAdjustment("protection_potion", Constants.PROTECTION_POTION_PRICE/100));
		recipe3.addIngredient(CurrencyManager.currency(Constants.PROTECTION_POTION_PRICE/100));
		recipes.add(recipe3);

        MerchantRecipe recipe4 = new MerchantRecipe(CDI.SpearPotion(), this.SpearStock);
        recipe4.setSpecialPrice(EM.getPriceAdjustment("spear_potion", Constants.SPEAR_POTION_PRICE/100));
        recipe4.addIngredient(CurrencyManager.currency(Constants.SPEAR_POTION_PRICE/100));
        recipes.add(recipe4);

        MerchantRecipe recipe5 = new MerchantRecipe(CDI.SpearPotion(), this.SaveStock);
        recipe5.setSpecialPrice(EM.getPriceAdjustment("death_potion", Constants.DEATH_SAVER_PRICE/100));
        recipe5.addIngredient(CurrencyManager.currency(Constants.DEATH_SAVER_PRICE/100));
        recipes.add(recipe5);

		merchant.setRecipes(recipes);
		viewer.openMerchant(merchant, true);
    }
    @Contract("_ -> _")
	public @Nullable Player getOwner(@Nullable Merchant merchant) {
		return shopOwners.getOrDefault(merchant, null);
	}

	public void removeShop(Merchant merchant) {
		shopOwners.remove(merchant);
	}
}
