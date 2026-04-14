package org.lazberry.xmasLegacy.FirstRoleManager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.XmasLegacy;

public class Mage extends AbstractFirstRole {

	public Mage(int c1, int c2, XmasLegacy plugin) {
		super(c1, c2, plugin);
	}

	@Override
	public void useFirstSkill(Player player) {

	}

	@Override
	public void useSecondSkill(Player player) {

	}

	@Override
	public Roles getRole() {
		return Roles.Mage;
	}

	@Override
	public ItemStack roleWeapon() {
		return null;
	}

    @Override
    public ItemStack roleArmor() {
        return null;
    }
}
