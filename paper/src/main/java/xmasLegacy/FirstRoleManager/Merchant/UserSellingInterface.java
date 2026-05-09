package xmasLegacy.FirstRoleManager.Merchant;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.Economy.Currency.CurrencyManager;
import xmasLegacy.FirstRoleManager.Farmer.AgeableCrops;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;

public class UserSellingInterface {
    private final Merchant inv;
    private final UserManager um;
    private final XmasLegacy plugin = JavaPlugin.getPlugin(XmasLegacy.class);

    public UserSellingInterface(Player viewer) {
        this.um = this.plugin.UM;
        this.inv = Bukkit.createMerchant(ColorUtils.chat("&c&lSystem - &7&l매입"));
        List<MerchantRecipe> recipes = new ArrayList<>();

        switch (um.getUser(viewer.getUniqueId()).getRole()) {
            case FARMER -> {
                ItemStack resultForWheat = CurrencyManager.currency(3);
                MerchantRecipe wheat = new MerchantRecipe(resultForWheat, Integer.MAX_VALUE);
                wheat.addIngredient(new ItemStack(Material.WHEAT, 16));
                recipes.add(wheat);


                ItemStack seed = AgeableCrops.SunFlowerSeed();
                seed.setAmount(32);
                MerchantRecipe seedRecipe = new MerchantRecipe(seed, Integer.MAX_VALUE);
                ItemStack money = CurrencyManager.currency(18);
                seedRecipe.addIngredient(money);
                recipes.add(seedRecipe);


                ItemStack resultForSunflower = CurrencyManager.currency(10);
                MerchantRecipe sunflower = new MerchantRecipe(resultForSunflower, Integer.MAX_VALUE);
                ItemStack ingredient = AgeableCrops.SunFlower();
                ingredient.setAmount(16);
                sunflower.addIngredient(ingredient);
                recipes.add(sunflower);
            }
            case MINER -> {

            }
        }
    }
    public @NotNull Merchant getInventory() {
        return this.inv;
    }
}
