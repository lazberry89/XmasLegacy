package org.lazberry.xmaslegacy.shop.goods;

import lombok.Data;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.utils.ColorUtils;

@Data
public class Goods {
	private final String name;
	private final ItemStack item;
	private final int initialPrice;
	private final boolean changeName;
	private int price;

	public Goods(String name, ItemStack item, int initialPrice, boolean changeName) {
		this.name = name;
		this.item = item;
		this.initialPrice = initialPrice;
		this.price = initialPrice;
		this.changeName = changeName;

		if (changeName) item.editMeta(m -> m.displayName(ColorUtils.chat(name)));
	}

	public Goods(String name, ItemStack item, int initialPrice) {
		this(name, item, initialPrice, false);
	}

	public boolean isDiscounted() {
		return initialPrice > price;
	}

	public boolean isIncreased() {
		return price > initialPrice;
	}

	public int changePrice(double percent) {
		price = (int) (percent * price);
		return price;
	}

	public NamedTextColor getColorByRate() {
		if (isDiscounted()) return NamedTextColor.BLUE;
		if (isIncreased()) return NamedTextColor.RED;
		return NamedTextColor.GREEN;
	}
}
