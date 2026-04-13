package org.lazberry.xmasLegacy.FirstRoleManager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmasLegacy.Roles.Roles;

public class Warrior extends AbstractFirstRole {

	public Warrior(int c1, int c2) {
		super(c1, c2);
	}

	@Override
	public void useFirstSkill(Player player) {

	}

	@Override
	public void useSecondSkill(Player player) {

	}

	@Override
	public Roles getRole() {
		return Roles.Warrior;
	}

	@Override
	public ItemStack roleItem() {
		return null;
	}
}
