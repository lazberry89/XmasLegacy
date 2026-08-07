package org.lazberry.xmaslegacy.stock;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

@ConsumableClass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Stock {
	@EqualsAndHashCode.Include
	private final @Getter String name;
	private final @Getter double maxPrice;
	private final @Getter double minPrice;
	private @Getter double currentPrice;

	public Stock(String name, double initPrice, double maxPrice, double minPrice) {
		this.name = name;
		this.currentPrice = initPrice;
		this.maxPrice = maxPrice;
		this.minPrice = minPrice;
	}

	public void applyFluctuation(double rate) {
		double nextPrice = this.currentPrice * (1 + rate);
		this.currentPrice = Math.max(minPrice, Math.min(maxPrice, nextPrice));
	}
}
