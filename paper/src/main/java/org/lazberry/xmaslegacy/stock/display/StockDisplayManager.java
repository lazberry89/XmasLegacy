package org.lazberry.xmaslegacy.stock.display;

import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Registry.Include(type = ServerType.MAIN)
public class StockDisplayManager {
	private final List<StockDisplay> displays = new ArrayList<>();

	public StockDisplayManager() {}

	public Collection<StockDisplay> snapshot() {
		return Collections.unmodifiableCollection(displays);
	}

	public boolean add(StockDisplay display) {
		display.spawn();
		return displays.add(display);
	}

	public <C extends Collection<StockDisplay>> boolean addAll(C values) {
		values.forEach(StockDisplay::spawn);
		return displays.addAll(values);
	}

	public boolean remove(StockDisplay display) {
		display.remove();
		return displays.remove(display);
	}

	public boolean removeRecent() {
		if (displays.isEmpty()) return false;
		var recent = displays.getLast();
		recent.remove();

		return displays.remove(recent);
	}

	public void clear() {
		displays.forEach(StockDisplay::remove);
		displays.clear();
	}

	public void updateAll() {
		displays.forEach(StockDisplay::update);
	}
}
