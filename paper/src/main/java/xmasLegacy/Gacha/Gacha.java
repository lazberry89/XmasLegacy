package xmasLegacy.Gacha;

import org.bukkit.inventory.ItemStack;

public class Gacha {
	private final ItemStack item;
	private double chance;

	public Gacha(ItemStack item, double chance) {
		this.item = item;
		this.chance = chance;
	}

	public ItemStack getItem() {
		return this.item;
	}
	public double getChance() {
		return this.chance;
	}
	public void setChance(double chance) {
		this.chance = chance;
	}
}
