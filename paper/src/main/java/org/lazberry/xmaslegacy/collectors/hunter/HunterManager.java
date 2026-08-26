package org.lazberry.xmaslegacy.collectors.hunter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HunterManager {
	private final XmasLegacy plugin;
	private final HunterRepository repository;
	private final Map<UUID, ActiveHunter> activeHunters = new ConcurrentHashMap<>();

	@Inject
	public HunterManager(XmasLegacy plugin, HunterRepository repository) {
		this.plugin = plugin;
		this.repository = repository;
	}


}
