package xmasLegacy.FirstRoleManager.Miner;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class Miner extends AbstractFirstRole {

	public Miner(int c1, int c2, XmasLegacy plugin) {
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
		return Roles.MINER;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.IRON_PICKAXE)
				.setName(ColorUtils.chat("&l광부의 곡괭이"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.setTag("role_id", "miner")
				.hideAllFlags()
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), Material.IRON_CHESTPLATE)
				.setName(ColorUtils.chat("&7&l철제 보호구"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.setTag("role_id", "miner")
				.hideAllFlags()
				.build()
				.clone();
	}


}
