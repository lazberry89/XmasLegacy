package org.lazberry.xmaslegacy.casino.logics;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.games.CoinFlip.CoinFlipManager;
import org.lazberry.xmaslegacy.casino.games.SlotMachine.SlotMachine;
import org.lazberry.xmaslegacy.casino.games.SlotMachine.SlotMachineManager;
import org.lazberry.xmaslegacy.casino.games.SlotMachine.Symbol;
import org.lazberry.xmaslegacy.casino.versus.VersusManager;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Keeps every slot machine, coin flip machine and the versus arena in plugins/XmasLegacy/casino.yml.
 * The /casino admin commands change the managers and call the matching save method here.
 */
@Slf4j
@Components.Include(type = ServerType.MAIN)
public class CasinoConfig implements Initiator {
	public static final List<String> VERSUS_POINTS = List.of("blue", "red", "blueroom", "redroom", "entrance");

	private final File file;
	private final SlotMachineManager slots;
	private final CoinFlipManager coinFlips;
	private final VersusManager versus;
	private YamlConfiguration yml = new YamlConfiguration();

	@Inject
	public CasinoConfig(XmasLegacy plugin, SlotMachineManager slots, CoinFlipManager coinFlips, VersusManager versus) {
		this.file = new File(plugin.getDataFolder(), "casino.yml");
		this.slots = slots;
		this.coinFlips = coinFlips;
		this.versus = versus;
	}

	@Override
	public void init() {
		yml = YamlConfiguration.loadConfiguration(file);
		loadSlots();
		loadCoinFlips();
		applyVersusField();
		log.info("Casino loaded: {} slot machine(s), {} coin flip machine(s), versus arena {}.",
				slots.getMachines().size(), coinFlips.getMachines().size(), versus.getField().isPresent() ? "ready" : "not set");
	}

	/* ---------- slot machines ---------- */

	private void loadSlots() {
		ConfigurationSection section = yml.getConfigurationSection("slots");
		if (section == null) return;
		for (String id : section.getKeys(false)) {
			ConfigurationSection s = section.getConfigurationSection(id);
			Location trigger = s == null ? null : location(s.get("trigger"));
			if (trigger == null) {
				log.warn("Casino: slot machine '{}' skipped, its lever location or world is missing.", id);
				continue;
			}
			SlotMachine machine = slots.createSlotMachine(id, trigger, s.getInt("cost"),
					s.getDouble("silver"), s.getDouble("gold"), s.getDouble("diamond"));
			List<?> reels = s.getList("reels", List.of());
			for (int i = 0; i < Math.min(3, reels.size()); i++) machine.setReel(i + 1, location(reels.get(i)));
		}
	}

	public void saveSlot(SlotMachine m) {
		String path = "slots." + m.getName();
		yml.set(path + ".trigger", str(m.getTriggerLocation()));
		yml.set(path + ".cost", m.getNeededCoin());
		yml.set(path + ".silver", m.getMultipliesPerSymbol(Symbol.SILVER));
		yml.set(path + ".gold", m.getMultipliesPerSymbol(Symbol.GOLD));
		yml.set(path + ".diamond", m.getMultipliesPerSymbol(Symbol.DIAMOND));
		yml.set(path + ".reels", Arrays.stream(m.getReels()).map(CasinoConfig::str).toList());
		save();
	}

	public void removeSlot(String id) {
		yml.set("slots." + id, null);
		save();
	}

	/* ---------- coin flips ---------- */

	private void loadCoinFlips() {
		ConfigurationSection section = yml.getConfigurationSection("coinflips");
		if (section == null) return;
		for (String id : section.getKeys(false)) {
			Location loc = location(section.get(id));
			if (loc == null) {
				log.warn("Casino: coin flip '{}' skipped, its location or world is missing.", id);
				continue;
			}
			coinFlips.registerMachine(id, loc, Casino.coin(1));
		}
	}

	public void saveCoinFlip(String id, Location loc) {
		yml.set("coinflips." + id, str(loc));
		save();
	}

	public void removeCoinFlip(String id) {
		yml.set("coinflips." + id, null);
		save();
	}

	/* ---------- versus arena ---------- */

	/** Stores one arena point; returns the points still missing. The arena goes live once none are missing. */
	public List<String> setVersusPoint(String point, Location loc) {
		yml.set("versus." + point, str(loc));
		save();
		applyVersusField();
		return missingVersusPoints();
	}

	public List<String> missingVersusPoints() {
		return VERSUS_POINTS.stream().filter(p -> location(yml.get("versus." + p)) == null).toList();
	}

	public void saveFences() {
		versus.getField().ifPresent(f -> {
			yml.set("versus.fences", f.getBreakableFences().stream().map(CasinoConfig::str).toList());
			yml.set("versus.fence-material", f.getRestoreMaterial().name());
		});
		save();
	}

	private void applyVersusField() {
		if (!missingVersusPoints().isEmpty()) return;
		Location[] p = VERSUS_POINTS.stream().map(k -> location(yml.get("versus." + k))).toArray(Location[]::new);
		versus.registerField(p[0], p[1], p[2], p[3], p[4], true);
		versus.getField().ifPresent(f -> {
			yml.getList("versus.fences", List.of()).stream()
					.map(CasinoConfig::location).filter(Objects::nonNull)
					.forEach(loc -> f.addFence(loc.getBlock().getLocation()));
			Material material = Material.matchMaterial(yml.getString("versus.fence-material", ""));
			if (material != null) f.setRestoreMaterial(material);
		});
	}

	/* ---------- helpers ---------- */

	// Stored as "world,x,y,z,yaw,pitch" text: Bukkit's own Location serialization makes the whole file
	// fail to load when one world (e.g. a Multiverse world) is not loaded yet.
	static String str(@Nullable Location l) {
		if (l == null || l.getWorld() == null) return "";
		return String.join(",", l.getWorld().getName(), String.valueOf(l.getX()), String.valueOf(l.getY()),
				String.valueOf(l.getZ()), String.valueOf(l.getYaw()), String.valueOf(l.getPitch()));
	}

	static @Nullable Location location(@Nullable Object value) {
		if (!(value instanceof String text) || text.isBlank()) return null;
		String[] p = text.split(",");
		if (p.length != 6) return null;
		World world = Bukkit.getWorld(p[0]);
		if (world == null) return null;
		try {
			return new Location(world, Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]),
					Float.parseFloat(p[4]), Float.parseFloat(p[5]));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void save() {
		try {
			yml.save(file);
		} catch (IOException e) {
			log.error("Casino: failed to save {}. Recent casino setup changes will be lost on restart.", file, e);
		}
	}
}
