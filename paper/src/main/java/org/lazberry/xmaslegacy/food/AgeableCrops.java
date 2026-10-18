package org.lazberry.xmaslegacy.food;

import io.th0rgal.oraxen.api.OraxenItems;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class AgeableCrops implements Initiator {

	public static @NotNull ItemStack SunFlowerBread() {
		var builder = OraxenItems.getItemById("sunflower_bread");
		var item = builder == null ? new ItemStack(Material.BREAD) : builder.build();

		ItemStack bread = ItemBuilder.of(XmasLegacy.getInstance(), item)
				.name(ColorUtils.chat("&e&l태양초 빵"))
				.lore(ColorUtils.chat("&7농부만이 만들 수 있는 이 세상을 살아갈 식량입니다."))
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

	public boolean registerRecipe() {
		NamespacedKey key = KeyUtils.get("sunflower_bread");

		if (Bukkit.getRecipe(key) != null) {
			Bukkit.removeRecipe(key);
		}
		ItemStack result = SunFlowerBread();
		result.setAmount(3);

		ShapedRecipe recipe = new ShapedRecipe(key, result);
		recipe.shape("WSW");
		recipe.setIngredient('S', Material.TORCHFLOWER);
		recipe.setIngredient('W', Material.WHEAT);

		return Bukkit.addRecipe(recipe);
	}

	@Override
	public void init() {
		if (registerRecipe()) log.info("Successfully registered SunflowerBread recipe.");
		else log.warn("Failed to register SunflowerBread recipe.");
	}
}
