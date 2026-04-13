package org.lazberry.xmasLegacy.FirstRoleManager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmasLegacy.Roles.Roles;

public abstract class AbstractFirstRole {
	private int cooldown1;
	private int cooldown2;

	public AbstractFirstRole(int c1, int c2) {
		this.cooldown1 = c1;
		this.cooldown2 = c2;
	}

	public abstract void useFirstSkill(Player player);
	public abstract void useSecondSkill(Player player);
	public abstract Roles getRole();
	public abstract ItemStack roleItem();

	public int getCooldown1() {
		return cooldown1;
	}

	public int getCooldown2() {
		return cooldown2;
	}
	public void loadCooldown(String path) {}
}
