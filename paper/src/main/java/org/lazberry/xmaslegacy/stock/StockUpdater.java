package org.lazberry.xmaslegacy.stock;

import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor
public class StockUpdater {

	public void tickPrices(Collection<Stock> stocks, double totalSpread, double negativeOffset) {
		for (Stock stock : stocks) {
			double raw = (Math.random() * 2.0) - 1.0;

			double cubicSpread = Math.pow(raw, 3);
			double randomRate = (cubicSpread * totalSpread) - negativeOffset;

			stock.applyFluctuation(randomRate);
		}
	}
}
