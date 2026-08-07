package org.lazberry.xmaslegacy.stock;

import lombok.NoArgsConstructor;

import java.util.Collection;

@NoArgsConstructor
public class StockUpdater {

	public void tickPrices(Collection<Stock> stocks) {
		for (Stock stock : stocks) {
			double randomRate = (Math.random() * 0.40) - 0.20;
			stock.applyFluctuation(randomRate);
		}
	}

}
