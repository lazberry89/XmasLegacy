package org.lazberry.xmaslegacy.stock;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

@Data
@ConsumableClass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Stock {
	@EqualsAndHashCode.Include
	private final String name;
	private final double maxPrice;
	private final double minPrice;
	private final double initPrice;
	private double currentPrice;
	private double previousPrice;

	public Stock(String name, double initPrice) {
		this(name, initPrice, initPrice * 6.0, Math.max(1.0, initPrice * 0.05));
	}

	public Stock(String name, double initPrice, double maxPrice, double minPrice) {
		this.name = name;
		this.initPrice = initPrice;
		this.currentPrice = initPrice;
		this.previousPrice = initPrice;
		this.maxPrice = maxPrice;
		this.minPrice = minPrice;
	}

	public void applyFluctuation(double rate) {
		previousPrice = currentPrice;
		double nextPrice = currentPrice * (1 + rate);
		currentPrice = Math.clamp(nextPrice, minPrice, maxPrice);
	}

	public Fluctuation defineChange() {
		if (currentPrice > previousPrice) return Fluctuation.RISE;
		if (currentPrice < previousPrice) return Fluctuation.DESCENT;
		return Fluctuation.STABLE;
	}

	public double getChangeRate() {
		if (previousPrice == 0) return 0.0;
		return ((currentPrice - previousPrice) / previousPrice) * 100.0;
	}

	public Component getFormatComponentMessage() {
		return switch (defineChange()) {
            case RISE -> ColorUtils.chat(String.format("&7&l%s &c&l▲ +%.2f%%", getName(), getChangeRate()));
			case DESCENT -> ColorUtils.chat(String.format("&7&l%s &9&l▼ %.2f%%", getName(), getChangeRate()));
			case STABLE -> ColorUtils.chat(String.format("&7&l%s - 0.0%%", getName()));
		};
	}

	public String getFormatStringMessage() {
		return switch (defineChange()) {
			case RISE -> String.format("&7&l%s &c&l▲ +%.2f%%", getName(), getChangeRate());
			case DESCENT -> String.format("&7&l%s &9&l▼ %.2f%%", getName(), getChangeRate());
			case STABLE -> String.format("&7&l%s - 0.0%%", getName());
		};
	}

	public Component getInfoMessage() {
		String changeSymbol = switch (defineChange()) {
			case RISE -> "&c▲";
			case DESCENT -> "&9▼";
			case STABLE -> "&7-";
		};

		String changeColor = switch (defineChange()) {
			case RISE -> "&c";
			case DESCENT -> "&9";
			case STABLE -> "&7";
		};

		return ColorUtils.chat(String.format(
				"""
                &6&l[ &f&l%s 종목 상세 정보 &6&l]
                &7&m--------------------------------
                &f• 현재 주가 : &e%,.0f원 &7(%s%s %.2f%%&7)
                &f• 전일 주가 : &7%,.0f원
                &f• 상장 주가 : &7%,.0f원
                
                &f• 상한가 : &c%,.0f원
                &f• 하한가 : &9%,.0f원
                &7&m--------------------------------
                """,
				name,
				currentPrice,
				changeColor,
				changeSymbol,
				Math.abs(getChangeRate()),
				previousPrice,
				initPrice,
				maxPrice,
				minPrice
		));
	}
}
