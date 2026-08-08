package org.lazberry.xmaslegacy.stock;

import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor
public class StockUpdater {

	public void tickPrices(Collection<Stock> stocks, double totalSpread, double negativeOffset) {
		for (Stock stock : stocks) {
			double randomRate = (Math.random() * totalSpread) - negativeOffset;
			stock.applyFluctuation(randomRate);
		}
	}
}
