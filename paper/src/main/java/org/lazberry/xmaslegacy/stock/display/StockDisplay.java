package org.lazberry.xmaslegacy.stock.display;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.Axiom;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;
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
		this.location.setPitch(0.0f);
		this.stocks = stocks;
	}

	@NotNull
	public TextDisplay spawn() {
		if (this.display != null && this.display.isValid()) remove();

		this.display = location.getWorld().spawn(location, TextDisplay.class, t -> {
			t.getPersistentDataContainer().set(KeyUtils.get("stock_display"), PersistentDataType.BOOLEAN, true);
			t.setBillboard(Display.Billboard.FIXED);
			t.setDefaultBackground(false);
			t.setBackgroundColor(Color.fromARGB(100, 0, 0, 0));
			t.setAlignment(TextDisplay.TextAlignment.CENTER);
			t.text(buildText());
			GlowUtils.glow(t, NamedTextColor.YELLOW);
		});
		return this.display;
	}

	public void update() {
		if (!location.isChunkLoaded()) return;
		if (this.display == null || !this.display.isValid()) {
			spawn();
			return;
		}
		this.display.text(buildText());
	}

	private Component buildText() {
		Component txt =                  ColorUtils.chat("&8&m━━━━━━━━━━&r &e&l📈 주식 현황 &r&8&m━━━━━━━━━━");
		for (Stock stock : stocks) {
			Component line = stock.getFormatComponentMessage()
					.append(ColorUtils.chat(String.format(" &7| &e%,.0f원", stock.getCurrentPrice())));
			txt = txt.appendNewline().appendNewline().append(line);
		}
		txt = txt.appendNewline().append(ColorUtils.chat("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
		return txt;
	}

	public void remove() {
		if (this.display != null && this.display.isValid()) {
			this.display.remove();
			this.display = null;
		}
	}
}
