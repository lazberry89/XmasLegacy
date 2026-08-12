package org.lazberry.xmaslegacy.stock.display;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Utils.Axiom;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;
import org.lazberry.xmaslegacy.stock.Stock;

@Getter
@ConsumableClass
public class StockDisplay {
	private final Stock[] stocks;
	private final Location location;
	private @Nullable TextDisplay display;

	public StockDisplay(Location loc, Stock ... stocks) {
		this.location = Axiom.snapDegrees(loc);
		this.stocks = stocks;
	}

	@NotNull
	public TextDisplay spawn() {
		if (this.display != null && this.display.isValid()) remove();

		this.display = location.getWorld().spawn(location, TextDisplay.class, t -> {
			t.setBillboard(Display.Billboard.FIXED);
			t.setDefaultBackground(false);
			t.setBackgroundColor(Color.fromARGB(100, 0, 0, 0));
			t.setAlignment(TextDisplay.TextAlignment.CENTER);
			t.text(buildText());
		});
		return this.display;
	}

	public void update() {
		if (this.display != null && this.display.isValid()) {
			this.display.text(buildText());
		}
	}

	private Component buildText() {
		Component txt = ColorUtils.chat("📉");
		for (Stock stock : stocks) {
			txt = txt.appendNewline()
					.append(stock.getFormatComponentMessage());
		}
		return txt;
	}

	public void remove() {
		if (this.display != null && this.display.isValid()) {
			this.display.remove();
			this.display = null;
		}
	}
}
