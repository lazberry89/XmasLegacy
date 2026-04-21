package org.lazberry.xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.lazberry.xmasLegacy.Economy.CurrencyManager;

import java.util.ArrayList;
import java.util.List;

public class PriestShop {
    private final ConductableItems CDI;

    public PriestShop(ConductableItems CDI) {
        this.CDI = CDI;
    }

    public void openShop() {
        Merchant merchant = Bukkit.createMerchant();

        List<MerchantRecipe> recipes = new ArrayList<>();
        MerchantRecipe recipe1 = new MerchantRecipe(CDI.DragonPotion(), 200);
        recipe1.addIngredient(CurrencyManager.currency(150));
        recipes.add(recipe1);

        MerchantRecipe recipe2 = new MerchantRecipe(CDI.HealerPotion(), 99);
        recipe2.addIngredient(CurrencyManager.currency(990));
        recipes.add(recipe2);


    }
}
