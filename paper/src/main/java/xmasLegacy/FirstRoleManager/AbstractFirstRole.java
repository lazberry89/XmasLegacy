package xmasLegacy.FirstRoleManager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.XmasLegacy;

public abstract class AbstractFirstRole {
	private final int cooldown1;
	private final int cooldown2;
    private final XmasLegacy plugin;

	public AbstractFirstRole(int c1, int c2, XmasLegacy plugin) {
		this.cooldown1 = c1;
		this.cooldown2 = c2;
		this.plugin = plugin;
	}

    public XmasLegacy getPlugin() {
        return this.plugin;
    }

	public abstract void useFirstSkill(Player p);
	public abstract void useSecondSkill(Player p);
	public abstract @NotNull Roles getRole();
	public abstract @NotNull ItemStack roleWeapon();
    public abstract @NotNull ItemStack roleArmor();

	public int getCooldown1() {
		return cooldown1;
	}

	public int getCooldown2() {
		return cooldown2;
	}
	public void loadCooldown(String path) {}

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean consumeEnergy(Player player, int hungerCost) {
        int currentFood = player.getFoodLevel();

        if (currentFood < hungerCost) {
            player.sendMessage(ColorUtils.chat(Prefix.RED + " 에너지가 부족하여 스킬을 사용할 수 없습니다! (필요: &6" + hungerCost + "&f)"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        player.setFoodLevel(Math.max(0, currentFood - hungerCost));
        player.setSaturation(0);

        return true;
    }
}
