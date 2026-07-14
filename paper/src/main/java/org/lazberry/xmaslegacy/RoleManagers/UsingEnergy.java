package org.lazberry.xmaslegacy.RoleManagers;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Utils.InfoUtils;

public interface UsingEnergy {

	/**
	 * In-game energy system.Using hunger as an energy system. This method handles decline, check function.
	 * <pre>{@code
	 * if (!consumeEnergy(p, 3)) return false;
	 * }</pre>
	 * @param player target player
	 * @param hungerCost how much cost to charge target player
	 * @return if check and decline process successes
	 * @see org.bukkit.event.entity.FoodLevelChangeEvent
     * @see InfoUtils#error(Player, Component)
	 * @see Player
	 */
	default boolean consumeEnergy(@NotNull Player player, int hungerCost) {
		int currentFood = player.getFoodLevel();

		if (currentFood < hungerCost) {
			InfoUtils.error(player, "에너지가 부족하여 스킬을 사용할 수 없습니다! (필요: &6{}&f)", hungerCost);
			return false;
		}
		player.setFoodLevel(Math.max(0, currentFood - hungerCost));

		return true;
	}
}
