package org.lazberry.xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.lazberry.xmasLegacy.Economy.CurrencyManager;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;

import java.util.*;

public class PriestShop {
    private final ConductableItems CDI;
	private final Map<Merchant, Player> shopOwners = new HashMap<>();

    public PriestShop(ConductableItems CDI) {
        this.CDI = CDI;
    }

    public void openShop(Player viewer, Player owner) {
        Merchant merchant = Bukkit.createMerchant();
		shopOwners.put(merchant, owner);

        List<MerchantRecipe> recipes = new ArrayList<>();
        MerchantRecipe recipe1 = new MerchantRecipe(CDI.DragonPotion(), 200);
        recipe1.addIngredient(CurrencyManager.currency(150));
        recipes.add(recipe1);

        MerchantRecipe recipe2 = new MerchantRecipe(CDI.HealerPotion(), 99);
        recipe2.addIngredient(CurrencyManager.currency(990));
        recipes.add(recipe2);

		MerchantRecipe recipe3 = new MerchantRecipe(CDI.ProtectionPotion(), 50);
		recipe3.addIngredient(CurrencyManager.currency(1650));
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
