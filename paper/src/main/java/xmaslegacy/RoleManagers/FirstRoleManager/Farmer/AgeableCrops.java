package xmaslegacy.RoleManagers.FirstRoleManager.Farmer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.Utils.ItemBuilder;
import xmaslegacy.XmasLegacy;

public class AgeableCrops {

	public static ItemStack SunFlower() {
		return ItemBuilder.of(XmasLegacy.getInstance(), Material.TORCHFLOWER)
				.setName(ColorUtils.chat("&6&l태양초"))
				.setLore(ColorUtils.chat("&7이 세상 모든 &e대백야&7의 사람들이 살아갈 수 있게 해준다."))
				.hideAllFlags()
				.setGlint(true)
				.setTag("farmer", "sunflower")
				.build()
				.clone();
	}

	public static ItemStack SunFlowerSeed() {
		return ItemBuilder.of(XmasLegacy.getInstance(), Material.TORCHFLOWER_SEEDS)
				.setName(ColorUtils.chat("&6&l태양초 씨앗"))
				.setLore(ColorUtils.chat("&7태양초를 기를 수 있는 고귀한 재료다."))
				.setTag("farmer", "sunflowerSeed")
				.setGlint(true)
				.hideAllFlags()
				.build()
				.clone();
	}

	public static ItemStack SunFlowerBread() {
		ItemStack bread = ItemBuilder.of(XmasLegacy.getInstance(), Material.BREAD)
				.setName(ColorUtils.chat("&e&l태양초 빵"))
				.setLore(ColorUtils.chat("&7농부만이 만들 수 있는 이 세상을 살아갈 식량입니다."))
				.setTag("farmer", "sunflower_bread")
				.hideAllFlags()
				.setGlint(true)
				.build();

		ItemMeta meta = bread.getItemMeta();
		if (meta != null) {
			FoodComponent food = meta.getFood();

			food.setNutrition(3);
			food.setSaturation(6.0f);
			food.setCanAlwaysEat(true);

			meta.setFood(food);
			bread.setItemMeta(meta);
		}

		return bread;
	}

	public static boolean RegisterRecipe(@NotNull XmasLegacy plugin) {
		NamespacedKey key = KeyUtils.get("sunflower_bread");

		if (Bukkit.getRecipe(key) != null) {
			Bukkit.removeRecipe(key);
		}

		ShapedRecipe recipe = new ShapedRecipe(key, SunFlowerBread());
		recipe.shape(" S ", " S ", " S ");
		recipe.setIngredient('S', new RecipeChoice.ExactChoice(SunFlower()));

		return Bukkit.addRecipe(recipe);
	}
}
