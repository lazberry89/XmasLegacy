package xmasLegacy.SecondaryRoleManager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.UsingEnergy;
import xmasLegacy.XmasLegacy;

@SuppressWarnings("BooleanMethodIsAlwaysInverted, DuplicatedCode, FieldCanBeLocal, unused, BooleanMethodIsAlwaysConverted")
public abstract class AbstractSecondRole implements UsingEnergy {
	private final XmasLegacy plugin;
	private final SecondaryRoles role;

	public AbstractSecondRole(SecondaryRoles role) {
		this.plugin = XmasLegacy.getInstance();
		this.role = role;
	}

	public abstract void useFirstSkill(Player p);
	public abstract void useSecondSkill(Player p);
	public abstract void usePassive(Player p);

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean consumeEnergy(Player player, int hungerCost) {
		int currentFood = player.getFoodLevel();

		if (currentFood < hungerCost) {
			player.sendMessage(ColorUtils.chat(Alert.RED + " 에너지가 부족하여 스킬을 사용할 수 없습니다! (필요: &6" + hungerCost + "&f)"));
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			return false;
		}

		player.setFoodLevel(Math.max(0, currentFood - hungerCost));

		return true;
	}
	public XmasLegacy getPlugin() {
		return this.plugin;
	}
	public abstract @NotNull Role getRole();
	public abstract @NotNull ItemStack roleWeapon();
	public abstract @NotNull ItemStack roleArmor();
}
