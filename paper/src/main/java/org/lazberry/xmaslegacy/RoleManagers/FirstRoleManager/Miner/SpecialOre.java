package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Miner;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;

public class SpecialOre {
	//강화석
	public static ItemStack GlacialShard() {
		return ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
				.setName(ColorUtils.chat("&e&l빙결파편(강화석)"))
				.setLore(ColorUtils.chat("&7강화에 사용하는 보석이다.가공이 어렵고 제작 또한 어렵다."))
				.setUnbreakable()
				.setGlint(true)
				//.setItemModel("upgrade")
				.setTag("upgrade", "true")
				.hideAllFlags()
				.build()
				.clone();
	}

	//Recipe
	public static boolean RegisterRecipe() {
		XmasLegacy plugin = JavaPlugin.getPlugin(XmasLegacy.class);
		NamespacedKey key = KeyUtils.get("glacier_shard");

		if (Bukkit.getRecipe(key) != null) {
			Bukkit.removeRecipe(key);
		}
		ShapedRecipe recipe = new ShapedRecipe(key, GlacialShard());
		recipe.shape(
				"SSS",
				"SDS",
				"SSS"
		);
		recipe.setIngredient('S', Material.IRON_INGOT);
		recipe.setIngredient('D', Material.DIAMOND);

		return Bukkit.addRecipe(recipe);
	}
}
