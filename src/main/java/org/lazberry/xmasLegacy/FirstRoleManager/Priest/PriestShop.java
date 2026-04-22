package org.lazberry.xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.lazberry.xmasLegacy.Economy.CurrencyManager;
import org.lazberry.xmasLegacy.Economy.EconomyManager;
import org.lazberry.xmasLegacy.Settings.Constants;

import java.util.*;

public class PriestShop {
    private final ConductableItems CDI;
    private final EconomyManager EM;
	private final Map<Merchant, Player> shopOwners = new HashMap<>();

    public PriestShop(ConductableItems CDI,  EconomyManager EM) {
        this.CDI = CDI;
        this.EM = EM;
    }

    public void openShop(Player viewer, Player owner) {
        Merchant merchant = Bukkit.createMerchant();
		shopOwners.put(merchant, owner);

        List<MerchantRecipe> recipes = new ArrayList<>();
        MerchantRecipe recipe1 = new MerchantRecipe(CDI.DragonPotion(), 200);
        recipe1.setSpecialPrice(EM.getPriceAdjustment("dragon_potion", Constants.DRAGON_BREATH_PRICE/100));
        recipe1.addIngredient(CurrencyManager.currency(Constants.DRAGON_BREATH_PRICE/100));
        recipes.add(recipe1);

        MerchantRecipe recipe2 = new MerchantRecipe(CDI.HealerPotion(), 99);
        recipe2.setSpecialPrice(EM.getPriceAdjustment("healer_potion", Constants.HEALER_POTION_PRICE/100));
        recipe2.addIngredient(CurrencyManager.currency(Constants.HEALER_POTION_PRICE/100));
        recipes.add(recipe2);

		MerchantRecipe recipe3 = new MerchantRecipe(CDI.ProtectionPotion(), 50);
        recipe3.setSpecialPrice(EM.getPriceAdjustment("protection_potion", Constants.PROTECTION_POTION_PRICE/100));
		recipe3.addIngredient(CurrencyManager.currency(Constants.PROTECTION_POTION_PRICE/100));
		recipes.add(recipe3);

		merchant.setRecipes(recipes);
		viewer.openMerchant(merchant, true);
    }

	public Player getOwner(Merchant merchant) {
		return shopOwners.getOrDefault(merchant, null);
	}

	public void removeShop(Merchant merchant) {
		shopOwners.remove(merchant);
	}
}
