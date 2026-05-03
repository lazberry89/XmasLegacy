package xmasLegacy.FirstRoleManager.Merchant;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Merchant extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.OPEN_STOCKS);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}
	public Merchant(int c1, int c2, XmasLegacy plugin) {
		super(c1, c2, plugin);
	}

	@Override
	public void useFirstSkill(Player p) {

	}

	@Override
	public void useSecondSkill(Player p) {

	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.MERCHANT;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.ENDER_CHEST)
				.setName(ColorUtils.chat("&d&l상인의 보자기"))
				.setLore(ColorUtils.chat("&7상점을 열거나 매입품을 확인할 수 있어요."))
				.hideAllFlags()
				.setTag("role_id", "merchant")
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return null;
	}


}
