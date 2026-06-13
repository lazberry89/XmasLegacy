package xmasLegacy;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;

@SuppressWarnings("DuplicatedCode")
public interface UsingEnergy {
	default boolean consumeEnergy(@NotNull Player player, int hungerCost) {
		int currentFood = player.getFoodLevel();

		if (currentFood < hungerCost) {
			player.sendMessage(ColorUtils.chat(Alert.RED + " 에너지가 부족하여 스킬을 사용할 수 없습니다! (필요: &6" + hungerCost + "&f)"));
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			return false;
		}

		player.setFoodLevel(Math.max(0, currentFood - hungerCost));

		return true;
	}
	void useDash(Player p);
}
