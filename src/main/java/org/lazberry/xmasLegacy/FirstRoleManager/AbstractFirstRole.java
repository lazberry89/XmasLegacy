package org.lazberry.xmasLegacy.FirstRoleManager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.XmasLegacy;

public abstract class AbstractFirstRole {
	private int cooldown1;
	private int cooldown2;
    private XmasLegacy plugin;

	public AbstractFirstRole(int c1, int c2, XmasLegacy plugin) {
		this.cooldown1 = c1;
		this.cooldown2 = c2;
		this.plugin = plugin;
	}

    public XmasLegacy getPlugin() {
        return this.plugin;
    }

	public abstract void useFirstSkill(Player player);
	public abstract void useSecondSkill(Player player);
	public abstract Roles getRole();
	public abstract ItemStack roleWeapon();
    public abstract ItemStack roleArmor();

	public int getCooldown1() {
		return cooldown1;
	}

	public int getCooldown2() {
		return cooldown2;
	}
	public void loadCooldown(String path) {}
}
