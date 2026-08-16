package org.lazberry.xmaslegacy.stock.display;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.stock.Stock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class StockDisplayManager {
	private final List<StockDisplay> displays = new ArrayList<>();
	private final XmasLegacy plugin;

	@Inject
	public StockDisplayManager(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	public int cleanDisplays(World world) {
		if (world == null) {
			log.error("World is null");
			return -1;
		}
		if (!Bukkit.isPrimaryThread())
			throw new IllegalStateException("cleanDisplays() must be called from the main thread!");
		synchronized (this) {
			List<TextDisplay> targets = world.getEntitiesByClass(TextDisplay.class)
					.stream()
					.filter(t -> t.getPersistentDataContainer().has(KeyUtils.get("stock_display")))
					.toList();

			targets.forEach(Entity::remove);
			return targets.size();
		}
	}

	public Collection<StockDisplay> snapshot() {
		return Collections.unmodifiableCollection(displays);
	}

	public boolean add(Location loc, Stock ... stocks) {
		return add(new StockDisplay(loc, stocks));
	}

	public boolean add(StockDisplay display) {
		runOnMainThread(display::spawn);
		return displays.add(display);
	}

	public <C extends Collection<StockDisplay>> boolean addAll(C values) {
		runOnMainThread(() -> values.forEach(StockDisplay::spawn));
		return displays.addAll(values);
	}

	public boolean remove(StockDisplay display) {
		runOnMainThread(display::remove);
		return displays.remove(display);
	}

	public boolean removeRecent() {
		if (displays.isEmpty()) return false;
		var recent = displays.getLast();
		runOnMainThread(recent::remove);

		return displays.remove(recent);
	}

	/**
	 * Must be called from the main thread (e.g. during plugin disable).
	 * Synchronously removes all display entities and clears the internal list.
	 */
	public void clearSync() {
		if (!Bukkit.isPrimaryThread())
			throw new IllegalStateException("clearSync() must be called from the main thread!");
		displays.forEach(StockDisplay::remove);
		displays.clear();
	}

	/** @deprecated Use clearSync() when on main thread to guarantee entity removal. */
	@Deprecated
	public void clear() {
		runOnMainThread(() -> displays.forEach(StockDisplay::remove));
		displays.clear();
	}

	public void updateAll() {
		runOnMainThread(() -> displays.forEach(StockDisplay::update));
	}

	private void runOnMainThread(Runnable runnable) {
		if (Bukkit.isPrimaryThread()) runnable.run();
		else Bukkit.getScheduler().runTask(plugin, runnable);
	}
}
