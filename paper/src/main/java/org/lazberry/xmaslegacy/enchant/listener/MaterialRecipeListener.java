package org.lazberry.xmaslegacy.enchant.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.enchant.material.MaterialRecipeManager;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;

import java.util.function.Supplier;

@Listeners
@Components.Exclude(type = ServerType.LOBBY)
public class MaterialRecipeListener implements Listener {

	/**
	 * 조합대에 아이템을 올릴 때 실시간으로 커스텀 PDC 메타데이터를 정밀 검증
	 * Paper 1.21 Data Components 환경에서 ExactChoice의 클라이언트 동기화 실패 문제를 완벽히 해결
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPrepareCraft(PrepareItemCraftEvent event) {
		Recipe recipe = event.getRecipe();
		if (recipe == null) return;
		if (!(recipe instanceof ShapedRecipe shaped)) return;

		NamespacedKey key = shaped.getKey();

		if (key.equals(MaterialRecipeManager.KEY_TOTEM)) {
			validateCraftingMatrix(event.getInventory(), 9, "essence_enchant", EnchantMaterial::TotemOfEnchant);
		} else if (key.equals(MaterialRecipeManager.KEY_FIVE_PERCENT)) {
			validateCraftingMatrix(event.getInventory(), 9, "totem_of_enchant", EnchantMaterial::BoosterFivePercent);
		} else if (key.equals(MaterialRecipeManager.KEY_TEN_PERCENT)) {
			validateCraftingMatrix(event.getInventory(), 4, "booster_five_percent", EnchantMaterial::BoosterTenPercent);
		}
	}

	/**
	 * 플레이어가 조합 결과물을 집어갈 때 위조 방지 최종 검증
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCraftItem(CraftItemEvent event) {
		Recipe recipe = event.getRecipe();
		if (recipe == null) return;
		if (!(recipe instanceof ShapedRecipe shaped)) return;

		NamespacedKey key = shaped.getKey();
		CraftingInventory inv = event.getInventory();

		if (key.equals(MaterialRecipeManager.KEY_TOTEM)) {
			if (!isMatrixValid(inv, 9, "essence_enchant")) {
				event.setCancelled(true);
			}
		} else if (key.equals(MaterialRecipeManager.KEY_FIVE_PERCENT)) {
			if (!isMatrixValid(inv, 9, "totem_of_enchant")) {
				event.setCancelled(true);
			}
		} else if (key.equals(MaterialRecipeManager.KEY_TEN_PERCENT)) {
			if (!isMatrixValid(inv, 4, "booster_five_percent")) {
				event.setCancelled(true);
			}
		}
	}

	private void validateCraftingMatrix(CraftingInventory inv, int requiredCount, String expectedTag, Supplier<ItemStack> resultSupplier) {
		if (isMatrixValid(inv, requiredCount, expectedTag)) {
			inv.setResult(resultSupplier.get());
		} else {
			inv.setResult(null);
		}
	}

	private boolean isMatrixValid(CraftingInventory inv, int requiredCount, String expectedTag) {
		ItemStack[] matrix = inv.getMatrix();
		int matchCount = 0;

		for (ItemStack item : matrix) {
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}

			// PDC 태그 일치 여부 확인
			if (!hasCustomTag(item, expectedTag)) {
				return false;
			}
			matchCount++;
		}

		return matchCount == requiredCount;
	}

	private boolean hasCustomTag(ItemStack item, String expectedTag) {
		if (item == null || !item.hasItemMeta()) {
			return false;
		}
		var pdc = item.getItemMeta().getPersistentDataContainer();
		String tag = pdc.get(EnchantMaterial.key, PersistentDataType.STRING);
		return expectedTag.equals(tag);
	}
}

