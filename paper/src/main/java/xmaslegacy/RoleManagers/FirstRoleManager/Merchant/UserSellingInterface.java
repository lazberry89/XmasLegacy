package xmaslegacy.RoleManagers.FirstRoleManager.Merchant;

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
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Economy.Currency.CurrencyManager;
import xmaslegacy.RoleManagers.FirstRoleManager.Farmer.AgeableCrops;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;

@Deprecated(since = "1.21.4")
@SuppressWarnings("FieldCanBeLocal, unused")
public class UserSellingInterface {
    private final Merchant inv;
    private final UserManager um;
    private final XmasLegacy plugin;
	private final Component title = ColorUtils.chat("&c&lSystem - &7&l매입");
	private final NamespacedKey key;

    public UserSellingInterface(Player viewer) {
        this.um = UserManager.INSTANCE;
		this.plugin = XmasLegacy.getInstance();
        this.inv = Bukkit.createMerchant(title);
		this.key = KeyUtils.get("merchant_money");
        List<MerchantRecipe> recipes = new ArrayList<>();

        switch (um.getUser(viewer.getUniqueId()).getRole()) {
            case BasicRoles.FARMER -> {
                ItemStack resultForWheat = CurrencyManager.currency(3);
	            ItemMeta meta = resultForWheat.getItemMeta();
				meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
				resultForWheat.setItemMeta(meta);
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
				ItemStack resultForCoal = CurrencyManager.currency(5);
	            ItemMeta metac = resultForCoal.getItemMeta();
	            metac.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
	            resultForCoal.setItemMeta(metac);
				MerchantRecipe coal = new MerchantRecipe(resultForCoal, Integer.MAX_VALUE);
				coal.addIngredient(new ItemStack(Material.COAL, 16));
				recipes.add(coal);

				ItemStack resultForIron = CurrencyManager.currency(11);
				ItemMeta metai = resultForIron.getItemMeta();
				metai.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
				resultForIron.setItemMeta(metai);
				MerchantRecipe iron = new MerchantRecipe(resultForIron, Integer.MAX_VALUE);
				iron.addIngredient(new ItemStack(Material.IRON_INGOT, 16));
				recipes.add(iron);

				ItemStack resultForGold = CurrencyManager.currency(13);
				ItemMeta metag = resultForGold.getItemMeta();
				metag.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
				resultForGold.setItemMeta(metag);
				MerchantRecipe gold = new MerchantRecipe(resultForGold, Integer.MAX_VALUE);
				gold.addIngredient(new ItemStack(Material.GOLD_INGOT, 16));
				recipes.add(gold);

				ItemStack resultForDiamond = CurrencyManager.currency(18);
				ItemMeta metad = resultForDiamond.getItemMeta();
				metad.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
				resultForDiamond.setItemMeta(metad);
				MerchantRecipe diamond = new MerchantRecipe(resultForDiamond, Integer.MAX_VALUE);
				diamond.addIngredient(new ItemStack(Material.DIAMOND, 8));
				recipes.add(diamond);
            }
	        default -> {
		        InfoUtils.infoMsg(InfoLevel.WARN, viewer, "해당 직업이 아니네요!");
				viewer.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
	        }
        }
		this.inv.setRecipes(recipes);
    }
	public Component getTitle() {return this.title;}

    public @NotNull Merchant getInventory() {
        return this.inv;
    }
}
