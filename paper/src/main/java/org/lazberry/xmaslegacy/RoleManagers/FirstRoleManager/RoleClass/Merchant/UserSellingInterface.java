package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Economy.Currency.CurrencyManager;
import org.lazberry.xmaslegacy.Env.AgeableCrops;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;

@Deprecated(since = "1.21.4")
public class UserSellingInterface {
    private final Merchant inv;
    private final XmasLegacy plugin;
	@Getter
	private final Component title = ColorUtils.chat("&c&lSystem - &7&l매입");

	public UserSellingInterface(Player viewer, UserManager um) {
		this.plugin = XmasLegacy.getInstance();
        this.inv = Bukkit.createMerchant(title);
		NamespacedKey key = KeyUtils.get("merchant_money");
        List<MerchantRecipe> recipes = new ArrayList<>();

		var user = um.getUser(viewer.getUniqueId());
		if (user == null) return;

        switch (user.getRole()) {
            case BasicRoles.FARMER -> {
                ItemStack resultForWheat = CurrencyManager.currency(plugin);
				resultForWheat.setAmount(3);
	            ItemMeta meta = resultForWheat.getItemMeta();
				meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
				resultForWheat.setItemMeta(meta);
                MerchantRecipe wheat = new MerchantRecipe(resultForWheat, Integer.MAX_VALUE);
                wheat.addIngredient(new ItemStack(Material.WHEAT, 16));
                recipes.add(wheat);

                ItemStack seed = AgeableCrops.SunFlowerSeed();
                seed.setAmount(32);
                MerchantRecipe seedRecipe = new MerchantRecipe(seed, Integer.MAX_VALUE);
                ItemStack money = CurrencyManager.currency(plugin);
				money.setAmount(18);
                seedRecipe.addIngredient(money);
                recipes.add(seedRecipe);

                ItemStack resultForSunflower = CurrencyManager.currency(plugin);
				resultForSunflower.setAmount(10);
	            ItemMeta metas = resultForSunflower.getItemMeta();
	            metas.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
	            resultForSunflower.setItemMeta(metas);
                MerchantRecipe sunflower = new MerchantRecipe(resultForSunflower, Integer.MAX_VALUE);
                ItemStack ingredient = AgeableCrops.SunFlower();
                ingredient.setAmount(16);
                sunflower.addIngredient(ingredient);
                recipes.add(sunflower);
            }
            case BasicRoles.MINER -> {
				ItemStack resultForCoal = CurrencyManager.currency(plugin);
				resultForCoal.setAmount(5);
	            ItemMeta metac = resultForCoal.getItemMeta();
	            metac.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
	            resultForCoal.setItemMeta(metac);
				MerchantRecipe coal = new MerchantRecipe(resultForCoal, Integer.MAX_VALUE);
				coal.addIngredient(new ItemStack(Material.COAL, 16));
				recipes.add(coal);

				ItemStack resultForIron = CurrencyManager.currency(plugin);
				resultForIron.setAmount(11);
				ItemMeta metai = resultForIron.getItemMeta();
				metai.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
				resultForIron.setItemMeta(metai);
				MerchantRecipe iron = new MerchantRecipe(resultForIron, Integer.MAX_VALUE);
				iron.addIngredient(new ItemStack(Material.IRON_INGOT, 16));
				recipes.add(iron);

				ItemStack resultForGold = CurrencyManager.currency(plugin);
				resultForGold.setAmount(13);
				ItemMeta metag = resultForGold.getItemMeta();
				metag.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
				resultForGold.setItemMeta(metag);
				MerchantRecipe gold = new MerchantRecipe(resultForGold, Integer.MAX_VALUE);
				gold.addIngredient(new ItemStack(Material.GOLD_INGOT, 16));
				recipes.add(gold);

				ItemStack resultForDiamond = CurrencyManager.currency(plugin);
				resultForDiamond.setAmount(18);
				ItemMeta metad = resultForDiamond.getItemMeta();
				metad.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
				resultForDiamond.setItemMeta(metad);
				MerchantRecipe diamond = new MerchantRecipe(resultForDiamond, Integer.MAX_VALUE);
				diamond.addIngredient(new ItemStack(Material.DIAMOND, 8));
				recipes.add(diamond);
            }
	        default -> {
		        InfoUtils.warn(viewer, "해당 직업이 아니네요!");
				viewer.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
	        }
        }
		this.inv.setRecipes(recipes);
    }

	public @NotNull Merchant getInventory() {
        return this.inv;
    }
}
