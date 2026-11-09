package org.lazberry.xmaslegacy.enchant.material;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.lazberry.xmaslegacy.framework.InitiatePriority;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Priority;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.utils.KeyUtils;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class MaterialRecipeManager implements Initiator {

	public static final NamespacedKey KEY_TOTEM = KeyUtils.get("totem_of_enchant");
	public static final NamespacedKey KEY_FIVE_PERCENT = KeyUtils.get("booster_five_percent");
	public static final NamespacedKey KEY_TEN_PERCENT = KeyUtils.get("booster_ten_percent");

	@Override
	@Priority(InitiatePriority.DELAYED)
	public void init() {
		registerTotemOfEnchantRecipe();
		registerFivePercentRecipe();
		registerTenPercentRecipe();
		log.info("Material recipes registered successfully.");
	}

	@Override
	public void close() {
		Bukkit.removeRecipe(KEY_TOTEM);
		Bukkit.removeRecipe(KEY_FIVE_PERCENT);
		Bukkit.removeRecipe(KEY_TEN_PERCENT);
	}

	/**
	 * 1. 강화 결정 (TotemOfEnchant) 조합법
	 * - 재료: 강화의 정수 (EssenceOfEnchant - 베이스: GHAST_TEAR) 9개
	 */
	private void registerTotemOfEnchantRecipe() {
		if (Bukkit.getRecipe(KEY_TOTEM) != null) {
			Bukkit.removeRecipe(KEY_TOTEM);
		}

		ShapedRecipe recipe = new ShapedRecipe(KEY_TOTEM, EnchantMaterial.TotemOfEnchant());
		recipe.shape("SSS", "SSS", "SSS");
		recipe.setIngredient('S', Material.GHAST_TEAR);

		Bukkit.addRecipe(recipe);
	}

	/**
	 * 2. 확률 강화서 5% (BoosterFivePercent) 조합법
	 * - 재료: 강화 결정 (TotemOfEnchant - 베이스: DIAMOND) 9개
	 */
	private void registerFivePercentRecipe() {
		if (Bukkit.getRecipe(KEY_FIVE_PERCENT) != null) {
			Bukkit.removeRecipe(KEY_FIVE_PERCENT);
		}

		ShapedRecipe recipe = new ShapedRecipe(KEY_FIVE_PERCENT, EnchantMaterial.BoosterFivePercent());
		recipe.shape("SSS", "SSS", "SSS");
		recipe.setIngredient('S', Material.DIAMOND);

		Bukkit.addRecipe(recipe);
	}

	/**
	 * 3. 확률 강화서 10% (BoosterTenPercent) 조합법
	 * - 재료: 확률 강화서 5% (BoosterFivePercent - 베이스: PAPER) 4개 (2x2)
	 */
	private void registerTenPercentRecipe() {
		if (Bukkit.getRecipe(KEY_TEN_PERCENT) != null) {
			Bukkit.removeRecipe(KEY_TEN_PERCENT);
		}

		ShapedRecipe recipe = new ShapedRecipe(KEY_TEN_PERCENT, EnchantMaterial.BoosterTenPercent());
		recipe.shape("SS", "SS");
		recipe.setIngredient('S', Material.PAPER);

		Bukkit.addRecipe(recipe);
	}
}
