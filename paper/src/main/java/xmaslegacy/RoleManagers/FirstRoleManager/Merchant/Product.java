package xmaslegacy.RoleManagers.FirstRoleManager.Merchant;

import org.bukkit.inventory.ItemStack;

public class Product {
	private final ItemStack item;
	private int price;

	public Product(ItemStack item, int price) {
		this.item = item;
		this.price = price;
	}

	public ItemStack getItem() {
		return item;
	}

	public int getPrice() {
		return price;
	}

	public void addPrice(int amount) {this.price += amount;}
	public void removePrice(int amount) {this.price -= amount;}
	public void setPrice(int price) {this.price = price;}
}
